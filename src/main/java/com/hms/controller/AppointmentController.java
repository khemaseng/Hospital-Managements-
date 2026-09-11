package com.hms.controller;

import com.hms.model.Appointment;
import com.hms.model.AppointmentStatus;
import com.hms.service.AppointmentService;
import com.hms.util.DialogUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppointmentController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label resultCountLabel;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> colCode;
    @FXML private TableColumn<Appointment, String> colPatient;
    @FXML private TableColumn<Appointment, String> colDoctor;
    @FXML private TableColumn<Appointment, String> colDepartment;
    @FXML private TableColumn<Appointment, String> colDate;
    @FXML private TableColumn<Appointment, String> colTime;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, Void> colActions;

    private final AppointmentService appointmentService = new AppointmentService();
    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("appointmentCode"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getAppointmentDate().format(DATE_FORMAT)));
        colTime.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getAppointmentTime().format(TIME_FORMAT)));
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus().name()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().addAll("badge", badgeClass(status));
                setGraphic(badge);
            }
        });
        addActionButtons();

        statusFilter.getItems().addAll("All", "SCHEDULED", "COMPLETED", "CANCELLED", "NO_SHOW");
        statusFilter.setValue("All");

        appointmentTable.setItems(appointments);
        refresh();
    }

    private String badgeClass(String status) {
        return switch (status) {
            case "SCHEDULED" -> "badge-scheduled";
            case "COMPLETED" -> "badge-completed";
            case "CANCELLED" -> "badge-cancelled";
            default -> "badge-noshow";
        };
    }

    private void refresh() {
        List<Appointment> results = appointmentService.search(searchField.getText(), statusFilter.getValue());
        appointments.setAll(results);
        resultCountLabel.setText(results.size() + " appointment(s)");
    }

    @FXML
    private void handleFilterChanged(KeyEvent event) {
        refresh();
    }

    @FXML
    private void handleFilterChanged() {
        refresh();
    }

    @FXML
    private void handleAdd() {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/AppointmentFormDialog.fxml", "Book Appointment");
        AppointmentFormController controller = handle.controller();
        controller.setOnSaved(a -> refresh());
        handle.showAndWait();
    }

    private void handleComplete(Appointment appointment) {
        appointmentService.updateStatus(appointment.getId(), AppointmentStatus.COMPLETED);
        refresh();
    }

    private void handleCancel(Appointment appointment) {
        boolean confirmed = DialogUtil.confirm("Cancel Appointment",
                "Cancel the appointment for " + appointment.getPatientName() + " on " +
                        appointment.getAppointmentDate().format(DATE_FORMAT) + "?");
        if (confirmed) {
            appointmentService.cancelAppointment(appointment.getId());
            refresh();
        }
    }

    private void handleNoShow(Appointment appointment) {
        appointmentService.updateStatus(appointment.getId(), AppointmentStatus.NO_SHOW);
        refresh();
    }

    private void handleDelete(Appointment appointment) {
        boolean confirmed = DialogUtil.confirm("Delete Appointment",
                "Permanently delete this appointment record? This cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            appointmentService.deleteAppointment(appointment.getId());
            refresh();
        } catch (Exception e) {
            DialogUtil.showError("Delete Failed", "Could not delete this appointment.");
        }
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button completeBtn = new Button("Complete");
            private final Button noShowBtn = new Button("No-show");
            private final Button cancelBtn = new Button("Cancel");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(4, completeBtn, noShowBtn, cancelBtn, deleteBtn);

            {
                completeBtn.getStyleClass().add("btn-secondary");
                noShowBtn.getStyleClass().add("btn-secondary");
                cancelBtn.getStyleClass().add("btn-secondary");
                deleteBtn.getStyleClass().add("btn-danger");
                completeBtn.setStyle("-fx-font-size: 10px; -fx-padding: 4 8 4 8;");
                noShowBtn.setStyle("-fx-font-size: 10px; -fx-padding: 4 8 4 8;");
                cancelBtn.setStyle("-fx-font-size: 10px; -fx-padding: 4 8 4 8;");
                deleteBtn.setStyle("-fx-font-size: 10px; -fx-padding: 4 8 4 8;");
                completeBtn.setOnAction(e -> handleComplete(getTableView().getItems().get(getIndex())));
                noShowBtn.setOnAction(e -> handleNoShow(getTableView().getItems().get(getIndex())));
                cancelBtn.setOnAction(e -> handleCancel(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Appointment a = getTableView().getItems().get(getIndex());
                boolean editable = a.getStatus() == AppointmentStatus.SCHEDULED;
                completeBtn.setDisable(!editable);
                noShowBtn.setDisable(!editable);
                cancelBtn.setDisable(!editable);
                setGraphic(box);
            }
        });
    }
}
