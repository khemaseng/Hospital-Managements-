package com.hms.controller;

import com.hms.model.MedicalRecord;
import com.hms.service.MedicalRecordService;
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

public class MedicalRecordController {

    @FXML private TextField searchField;
    @FXML private Label resultCountLabel;
    @FXML private TableView<MedicalRecord> recordTable;
    @FXML private TableColumn<MedicalRecord, String> colDate;
    @FXML private TableColumn<MedicalRecord, String> colPatient;
    @FXML private TableColumn<MedicalRecord, String> colDoctor;
    @FXML private TableColumn<MedicalRecord, String> colDiagnosis;
    @FXML private TableColumn<MedicalRecord, String> colPrescription;
    @FXML private TableColumn<MedicalRecord, Void> colActions;

    private final MedicalRecordService recordService = new MedicalRecordService();
    private final ObservableList<MedicalRecord> records = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRecordDate() != null ? data.getValue().getRecordDate().format(DATE_FORMAT) : ""));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        colPrescription.setCellValueFactory(new PropertyValueFactory<>("prescription"));
        addActionButtons();

        recordTable.setItems(records);
        refresh();
    }

    private void refresh() {
        List<MedicalRecord> results = recordService.search(searchField.getText());
        records.setAll(results);
        resultCountLabel.setText(results.size() + " record(s)");
    }

    @FXML
    private void handleFilterChanged(KeyEvent event) {
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
