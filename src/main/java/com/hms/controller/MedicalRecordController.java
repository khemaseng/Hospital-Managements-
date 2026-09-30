
package com.hms.controller;

import com.hms.model.Doctor;
import com.hms.model.MedicalRecord;
import com.hms.service.DoctorService;
import com.hms.service.MedicalRecordService;
import com.hms.util.DialogUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedicalRecordController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> doctorFilter;
    @FXML private ComboBox<String> sortFilter;
    @FXML private Label resultCountLabel;
    @FXML private TableView<MedicalRecord> recordTable;
    @FXML private TableColumn<MedicalRecord, String> colDate;
    @FXML private TableColumn<MedicalRecord, String> colPatient;
    @FXML private TableColumn<MedicalRecord, String> colDoctor;
    @FXML private TableColumn<MedicalRecord, String> colDiagnosis;
    @FXML private TableColumn<MedicalRecord, String> colPrescription;
    @FXML private TableColumn<MedicalRecord, Void> colActions;

    private final MedicalRecordService recordService = new MedicalRecordService();
    private final DoctorService doctorService = new DoctorService();
    private final ObservableList<MedicalRecord> records = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRecordDate() != null ? data.getValue().getRecordDate().format(DATE_FORMAT) : ""));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        colPrescription.setCellValueFactory(new PropertyValueFactory<>("prescription"));
        addActionButtons();

        // 1. Load active Doctors into Doctor Filter
        loadDoctorFilters();

        // 2. Setup Sort Dropdown Options
        sortFilter.getItems().setAll(
                "Date (Newest)",
                "Date (Oldest)",
                "Patient (A-Z)",
                "Doctor (A-Z)"
        );
        sortFilter.setValue("Date (Newest)");

        // 3. Attach Direct Property Listeners (Real-time reactivity)
        doctorFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());
        sortFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refresh());

        recordTable.setItems(records);
        refresh();
    }

    private void loadDoctorFilters() {
        doctorFilter.getItems().clear();
        doctorFilter.getItems().add("All Doctors");
        try {
            List<Doctor> doctors = doctorService.getAllDoctors();
            for (Doctor d : doctors) {
                doctorFilter.getItems().add(d.getFullName());
            }
        } catch (Exception ignored) {}
        doctorFilter.setValue("All Doctors");
    }

    private void refresh() {
        String keyword = searchField != null ? searchField.getText() : "";
        String doctor = doctorFilter != null && doctorFilter.getValue() != null ? doctorFilter.getValue() : "All Doctors";
        String sortBy = sortFilter != null && sortFilter.getValue() != null ? sortFilter.getValue() : "Date (Newest)";

        List<MedicalRecord> results = recordService.search(keyword, doctor, sortBy);
        records.setAll(results);

        if (resultCountLabel != null) {
            resultCountLabel.setText(results.size() + " record(s)");
        }
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
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/MedicalRecordFormDialog.fxml", "Add Medical Record");
        MedicalRecordFormController controller = handle.controller();
        controller.setOnSaved(r -> refresh());
        handle.showAndWait();
    }

    private void handleEdit(MedicalRecord record) {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/MedicalRecordFormDialog.fxml", "Edit Medical Record");
        MedicalRecordFormController controller = handle.controller();
        controller.setRecordToEdit(record);
        controller.setOnSaved(r -> refresh());
        handle.showAndWait();
    }

    private void handleDelete(MedicalRecord record) {
        boolean confirmed = DialogUtil.confirm("Delete Medical Record",
                "Delete this medical record for " + record.getPatientName() + "? This cannot be undone.");
        if (!confirmed) {
            return;
        }
        try {
            recordService.deleteRecord(record.getId());
            refresh();
        } catch (Exception e) {
            DialogUtil.showError("Delete Failed", "Could not delete this medical record.");
        }
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, deleteBtn);

            {
                box.setAlignment(javafx.geometry.Pos.CENTER); // ឱ្យប្រអប់ប៊ូតុងនៅចំកណ្តាល
                editBtn.getStyleClass().add("btn-secondary");
                deleteBtn.getStyleClass().add("btn-danger");
                editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10 4 10;");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10 4 10;");
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