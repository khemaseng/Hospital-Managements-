package com.hms.controller;

import com.hms.model.Invoice;
import com.hms.model.PaymentStatus;
import com.hms.service.BillingService;
import com.hms.service.ExcelExportService;
import com.hms.util.DialogUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class BillingController {

    @FXML private TextField searchField;
    @FXML private Label totalRevenueLabel;
    @FXML private TableView<Invoice> invoiceTable;
    @FXML private TableColumn<Invoice, String> colCode;
    @FXML private TableColumn<Invoice, String> colPatient;
    @FXML private TableColumn<Invoice, String> colDate;
    @FXML private TableColumn<Invoice, String> colSubtotal;
    @FXML private TableColumn<Invoice, String> colDiscount;
    @FXML private TableColumn<Invoice, String> colTax;
    @FXML private TableColumn<Invoice, String> colTotal;
    @FXML private TableColumn<Invoice, String> colStatus;
    @FXML private TableColumn<Invoice, Void> colActions;

    private final BillingService billingService = new BillingService();
    private final ExcelExportService excelExportService = new ExcelExportService();
    private final ObservableList<Invoice> invoices = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("invoiceCode"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getIssuedOn() != null ? data.getValue().getIssuedOn().format(DATE_FORMAT) : ""));
        colSubtotal.setCellValueFactory(data -> money(data.getValue().getConsultationFee()
                + data.getValue().getMedicineFee() + data.getValue().getLabFee()));
        colDiscount.setCellValueFactory(data -> money(data.getValue().getDiscount()));
        colTax.setCellValueFactory(data -> money(data.getValue().getTax()));
        colTotal.setCellValueFactory(data -> money(data.getValue().getTotal()));
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPaymentStatus() != null ? data.getValue().getPaymentStatus().name() : "UNPAID"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().addAll("badge", switch (status) {
                    case "PAID" -> "badge-completed";
                    case "PARTIAL" -> "badge-noshow";
                    default -> "badge-cancelled";
                });
                setGraphic(badge);
            }
        });
        addActionButtons();

        invoiceTable.setItems(invoices);
        refresh();
    }

    private javafx.beans.property.SimpleStringProperty money(double amount) {
        return new javafx.beans.property.SimpleStringProperty(String.format(Locale.US, "$%,.2f", amount));
    }

    private void refresh() {
        List<Invoice> results = billingService.search(searchField.getText());
        invoices.setAll(results);
        double total = results.stream().mapToDouble(Invoice::getTotal).sum();
        totalRevenueLabel.setText(results.size() + " invoice(s)  •  " + String.format(Locale.US, "$%,.2f total", total));
    }

    @FXML
    private void handleFilterChanged(KeyEvent event) {
        refresh();
    }

    @FXML
    private void handleAdd() {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/InvoiceFormDialog.fxml", "New Invoice");
        InvoiceFormController controller = handle.controller();
        controller.setOnSaved(inv -> refresh());
        handle.showAndWait();
    }

    @FXML
    private void handleExportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Invoices to Excel");
        chooser.setInitialFileName("hms-invoices-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"));
        Stage stage = (Stage) invoiceTable.getScene().getWindow();
        java.io.File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        try {
            List<Invoice> all = billingService.search(searchField.getText());
            excelExportService.exportInvoices(all, file);
            DialogUtil.showInfo("Export Complete", all.size() + " invoice(s) exported to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            DialogUtil.showError("Export Failed", "Could not write the Excel file: " + e.getMessage());
        }
    }

    private void handleViewReceipt(Invoice inv) {
        String receipt = String.format(Locale.US,
                "HOSPITAL MANAGEMENT SYSTEM%n" +
                "Official Receipt%n" +
                "----------------------------------------%n" +
                "Invoice:   %s%n" +
                "Patient:   %s%n" +
                "Date:      %s%n" +
                "----------------------------------------%n" +
                "Consultation Fee:      $%,10.2f%n" +
                "Medicine Fee:          $%,10.2f%n" +
                "Laboratory Fee:        $%,10.2f%n" +
                "Discount:             -$%,10.2f%n" +
                "Tax:                   $%,10.2f%n" +
                "----------------------------------------%n" +
                "TOTAL:                 $%,10.2f%n" +
                "----------------------------------------%n" +
                "Thank you for choosing our hospital.",
                inv.getInvoiceCode(), inv.getPatientName(),
                inv.getIssuedOn() != null ? inv.getIssuedOn().format(DATE_FORMAT) : "",
                inv.getConsultationFee(), inv.getMedicineFee(), inv.getLabFee(),
                inv.getDiscount(), inv.getTax(), inv.getTotal());

        TextArea area = new TextArea(receipt);
        area.setEditable(false);
        area.setWrapText(false);
        area.setStyle("-fx-font-family: 'Consolas', 'Monospace';");
        area.setPrefRowCount(16);
        area.setPrefColumnCount(45);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Receipt - " + inv.getInvoiceCode());
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    private void handleDelete(Invoice inv) {
        boolean confirmed = DialogUtil.confirm("Delete Invoice",
                "Delete invoice " + inv.getInvoiceCode() + " for " + inv.getPatientName() + "?");
        if (!confirmed) {
            return;
        }
        try {
            billingService.deleteInvoice(inv.getId());
            refresh();
        } catch (Exception e) {
            DialogUtil.showError("Delete Failed", "Could not delete this invoice.");
        }
    }

    private void handleMarkPaid(Invoice inv) {
        try {
            billingService.updatePaymentStatus(inv.getId(), PaymentStatus.PAID);
            refresh();
        } catch (Exception e) {
            DialogUtil.showError("Update Failed", "Could not update the payment status.");
        }
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("Receipt");
            private final Button paidBtn = new Button("Mark Paid");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, viewBtn, paidBtn, deleteBtn);

            {
                viewBtn.getStyleClass().add("btn-secondary");
                paidBtn.getStyleClass().add("btn-secondary");
                deleteBtn.getStyleClass().add("btn-danger");
                viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10 5 10;");
                paidBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10 5 10;");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10 5 10;");
                viewBtn.setOnAction(e -> handleViewReceipt(getTableView().getItems().get(getIndex())));
                paidBtn.setOnAction(e -> handleMarkPaid(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Invoice inv = getTableView().getItems().get(getIndex());
                paidBtn.setDisable(inv.getPaymentStatus() == PaymentStatus.PAID);
                setGraphic(box);
            }
        });
    }
}
