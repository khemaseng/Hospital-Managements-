package com.hms.controller;

import com.hms.model.Doctor;
import com.hms.service.DoctorService;
import com.hms.service.ExcelExportService;
import com.hms.util.DialogUtil;
import com.hms.util.IconFactory;
import com.hms.util.Page;
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

public class DoctorController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private Label resultCountLabel;
    @FXML private TableView<Doctor> doctorTable;
    @FXML private TableColumn<Doctor, String> colCode;
    @FXML private TableColumn<Doctor, String> colName;
    @FXML private TableColumn<Doctor, String> colDepartment;
    @FXML private TableColumn<Doctor, String> colSpecialization;
    @FXML private TableColumn<Doctor, String> colPhone;
    @FXML private TableColumn<Doctor, Number> colFee;
    @FXML private TableColumn<Doctor, Void> colActions;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private ComboBox<Integer> pageSizeCombo;

    private final DoctorService doctorService = new DoctorService();
    private final ExcelExportService excelExportService = new ExcelExportService();
    private final ObservableList<Doctor> doctors = FXCollections.observableArrayList();

    private int currentPage = 0;
    private int pageSize = 8;

    // Single source of truth for the department list, shared with DoctorFormController
    // so "Add Doctor" and this filter never drift apart.
    public static final String[] DEPARTMENTS = {
            "General Medicine", "Cardiology", "Pediatrics", "Orthopedics",
            "Neurology", "Dermatology", "Radiology", "Surgery", "Psychiatry"
    };

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("doctorCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colFee.setCellValueFactory(new PropertyValueFactory<>("consultationFee"));
        addActionButtons();
        prevPageButton.setGraphic(IconFactory.chevronLeft(11, "icon-shape-dark"));
        nextPageButton.setGraphic(IconFactory.chevronRight(11, "icon-shape-dark"));
        nextPageButton.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);

        departmentFilter.getItems().add("All");
        departmentFilter.getItems().addAll(DEPARTMENTS);
        departmentFilter.setValue("All");
        pageSizeCombo.getItems().addAll(8, 15, 25, 50);
        pageSizeCombo.setValue(pageSize);

        doctorTable.setItems(doctors);
        refresh();
    }

    private void refresh() {
        Page<Doctor> page = doctorService.search(searchField.getText(), departmentFilter.getValue(), currentPage, pageSize);
        doctors.setAll(page.items());
        resultCountLabel.setText(page.totalItems() + " doctor(s)");
        pageInfoLabel.setText("Page " + (page.pageIndex() + 1) + " of " + page.totalPages());
        prevPageButton.setDisable(!page.hasPrevious());
        nextPageButton.setDisable(!page.hasNext());
    }

    @FXML
    private void handleFilterChanged(KeyEvent event) {
        currentPage = 0;
        refresh();
    }

    @FXML
    private void handleFilterChanged() {
        currentPage = 0;
        refresh();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            refresh();
        }
    }

    @FXML
    private void handleNextPage() {
        currentPage++;
        refresh();
    }

    @FXML
    private void handlePageSizeChanged() {
        pageSize = pageSizeCombo.getValue();
        currentPage = 0;
        refresh();
    }

    @FXML
    private void handleAdd() {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/DoctorFormDialog.fxml", "Add Doctor");
        DoctorFormController controller = handle.controller();
        controller.setOnSaved(d -> refresh());
        handle.showAndWait();
    }

    @FXML
    private void handleExportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Doctors to Excel");
        chooser.setInitialFileName("hms-doctors-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"));
        Stage stage = (Stage) doctorTable.getScene().getWindow();
        java.io.File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        try {
            Page<Doctor> all = doctorService.search(searchField.getText(), departmentFilter.getValue(), 0, Integer.MAX_VALUE);
            excelExportService.exportDoctors(all.items(), file);
            DialogUtil.showInfo("Export Complete", all.items().size() + " doctor(s) exported to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            DialogUtil.showError("Export Failed", "Could not write the Excel file: " + e.getMessage());
        }
    }

    private void handleEdit(Doctor doctor) {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/DoctorFormDialog.fxml", "Edit Doctor");
        DoctorFormController controller = handle.controller();
        controller.setDoctorToEdit(doctor);
        controller.setOnSaved(d -> refresh());
        handle.showAndWait();
    }

    private void handleDelete(Doctor doctor) {
        boolean confirmed = DialogUtil.confirm("Delete Doctor",
                "Are you sure you want to delete Dr. " + doctor.getFullName() + "? This cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            doctorService.deleteDoctor(doctor.getId());
            refresh();
        } catch (Exception e) {
            DialogUtil.showError("Delete Failed",
                    "Could not delete this doctor. They may have existing appointments.");
        }
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-secondary");
                deleteBtn.getStyleClass().add("btn-danger");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }
}
