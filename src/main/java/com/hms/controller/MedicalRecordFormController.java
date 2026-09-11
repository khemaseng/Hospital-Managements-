package com.hms.controller;

import com.hms.model.Doctor;
import com.hms.model.MedicalRecord;
import com.hms.model.Patient;
import com.hms.service.DoctorService;
import com.hms.service.MedicalRecordService;
import com.hms.service.PatientService;
import com.hms.util.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;

public class MedicalRecordFormController {

    @FXML private Label dialogTitleLabel;
    @FXML private Label formErrorLabel;
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private TextArea diagnosisField;
    @FXML private TextArea symptomsField;
    @FXML private TextArea prescriptionField;
    @FXML private TextArea labResultField;
    @FXML private TextArea notesField;
    @FXML private Button saveButton;

    private final PatientService patientService = new PatientService();
    private final DoctorService doctorService = new DoctorService();
    private final MedicalRecordService recordService = new MedicalRecordService();

    private MedicalRecord editingRecord; // null => add mode
    private Consumer<MedicalRecord> onSaved;

    @FXML
    public void initialize() {
        List<Patient> patients = patientService.getAllPatients();
        patientCombo.getItems().addAll(patients);
        patientCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Patient p) {
                return p == null ? "" : p.getFullName() + " (" + p.getPatientCode() + ")";
            }

            @Override
            public Patient fromString(String string) {
                return null;
            }
        });

        List<Doctor> doctors = doctorService.getAllDoctors();
        doctorCombo.getItems().addAll(doctors);
        doctorCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Doctor d) {
                return d == null ? "" : "Dr. " + d.getFullName() + " - " + d.getDepartment();
            }

            @Override
            public Doctor fromString(String string) {
                return null;
            }
        });
    }

    public void setOnSaved(Consumer<MedicalRecord> callback) {
        this.onSaved = callback;
    }

    public void setRecordToEdit(MedicalRecord record) {
        this.editingRecord = record;
        dialogTitleLabel.setText("Edit Medical Record");
        patientCombo.setDisable(true);
        doctorCombo.setDisable(true);
        patientCombo.getItems().stream().filter(p -> p.getId() == record.getPatientId()).findFirst()
                .ifPresent(patientCombo::setValue);
        doctorCombo.getItems().stream().filter(d -> d.getId() == record.getDoctorId()).findFirst()
                .ifPresent(doctorCombo::setValue);
        diagnosisField.setText(record.getDiagnosis());
        symptomsField.setText(record.getSymptoms());
        prescriptionField.setText(record.getPrescription());
        labResultField.setText(record.getLabResult());
        notesField.setText(record.getDoctorNotes());
        saveButton.setText("Update Record");
    }

    @FXML
    private void handleSave() {
        hideError();
        try {
            MedicalRecord record = editingRecord != null ? editingRecord : new MedicalRecord();
            Patient patient = patientCombo.getValue();
            Doctor doctor = doctorCombo.getValue();
            if (patient != null) {
                record.setPatientId(patient.getId());
            }
            if (doctor != null) {
                record.setDoctorId(doctor.getId());
            }
            record.setDiagnosis(diagnosisField.getText());
            record.setSymptoms(symptomsField.getText());
            record.setPrescription(prescriptionField.getText());
            record.setLabResult(labResultField.getText());
            record.setDoctorNotes(notesField.getText());

            if (editingRecord != null) {
                recordService.updateRecord(record);
            } else {
                recordService.addRecord(record);
            }

            if (onSaved != null) {
                onSaved.accept(record);
            }
            closeWindow();
        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("An unexpected error occurred while saving: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }

    private void showError(String message) {
        formErrorLabel.setText(message);
        formErrorLabel.setVisible(true);
        formErrorLabel.setManaged(true);
    }

    private void hideError() {
        formErrorLabel.setVisible(false);
        formErrorLabel.setManaged(false);
    }
}
