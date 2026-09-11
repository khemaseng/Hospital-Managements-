package com.hms.controller;

import com.hms.model.Patient;
import com.hms.service.PatientService;
import com.hms.util.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Period;
import java.util.function.Consumer;

/**
 * Backing controller for the Add/Edit Patient modal dialog. Works in both
 * "add" and "edit" mode depending on whether setPatientToEdit() was called
 * before the dialog is shown.
 */
public class PatientFormController {

    @FXML private Label dialogTitleLabel;
    @FXML private Label formErrorLabel;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private DatePicker dobPicker;
    @FXML private Spinner<Integer> ageSpinner;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextField emergencyContactField;
    @FXML private TextArea addressField;
    @FXML private Button saveButton;

    private final PatientService patientService = new PatientService();
    private Patient editingPatient; // null => add mode
    private Consumer<Patient> onSaved;

    @FXML
    public void initialize() {
        genderCombo.getItems().addAll("Male", "Female", "Other");
        bloodGroupCombo.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        ageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 150, 0));
    }

    public void setOnSaved(Consumer<Patient> callback) {
        this.onSaved = callback;
    }

    /** Pre-fills the form for editing an existing patient. */
    public void setPatientToEdit(Patient patient) {
        this.editingPatient = patient;
        dialogTitleLabel.setText("Edit Patient - " + patient.getPatientCode());
        nameField.setText(patient.getFullName());
        genderCombo.setValue(patient.getGender());
        dobPicker.setValue(patient.getDateOfBirth());
        ageSpinner.getValueFactory().setValue(patient.getAge());
        phoneField.setText(patient.getPhone());
        emailField.setText(patient.getEmail());
        bloodGroupCombo.setValue(patient.getBloodGroup());
        emergencyContactField.setText(patient.getEmergencyContact());
        addressField.setText(patient.getAddress());
        saveButton.setText("Update Patient");
    }

    @FXML
    private void handleDobChanged() {
        LocalDate dob = dobPicker.getValue();
        if (dob != null && !dob.isAfter(LocalDate.now())) {
            int computedAge = Period.between(dob, LocalDate.now()).getYears();
            ageSpinner.getValueFactory().setValue(computedAge);
        }
    }

    @FXML
    private void handleSave() {
        hideError();
        try {
            Patient patient = editingPatient != null ? editingPatient : new Patient();
            patient.setFullName(nameField.getText());
            patient.setGender(genderCombo.getValue());
            patient.setDateOfBirth(dobPicker.getValue());
            patient.setAge(ageSpinner.getValue());
            patient.setPhone(phoneField.getText());
            patient.setEmail(emailField.getText());
            patient.setBloodGroup(bloodGroupCombo.getValue());
            patient.setEmergencyContact(emergencyContactField.getText());
            patient.setAddress(addressField.getText());

            if (editingPatient != null) {
                patientService.updatePatient(patient);
            } else {
                patientService.addPatient(patient);
            }

            if (onSaved != null) {
                onSaved.accept(patient);
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
