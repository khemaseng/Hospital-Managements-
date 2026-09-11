package com.hms.controller;

import com.hms.model.Doctor;
import com.hms.service.DoctorService;
import com.hms.util.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class DoctorFormController {

    @FXML private Label dialogTitleLabel;
    @FXML private Label formErrorLabel;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> departmentCombo;
    @FXML private TextField specializationField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Spinner<Double> feeSpinner;
    @FXML private TextField scheduleField;
    @FXML private Button saveButton;

    private final DoctorService doctorService = new DoctorService();
    private Doctor editingDoctor; // null => add mode
    private Consumer<Doctor> onSaved;

    @FXML
    public void initialize() {
        departmentCombo.getItems().addAll(DoctorController.DEPARTMENTS);
        feeSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10000, 25, 5));
    }

    public void setOnSaved(Consumer<Doctor> callback) {
        this.onSaved = callback;
    }

    public void setDoctorToEdit(Doctor doctor) {
        this.editingDoctor = doctor;
        dialogTitleLabel.setText("Edit Doctor - " + doctor.getDoctorCode());
        nameField.setText(doctor.getFullName());
        departmentCombo.setValue(doctor.getDepartment());
        specializationField.setText(doctor.getSpecialization());
        phoneField.setText(doctor.getPhone());
        emailField.setText(doctor.getEmail());
        feeSpinner.getValueFactory().setValue(doctor.getConsultationFee());
        scheduleField.setText(doctor.getWorkingSchedule());
        saveButton.setText("Update Doctor");
    }

    @FXML
    private void handleSave() {
        hideError();
        try {
            // Commit any in-progress spinner text edit before reading the value.
            feeSpinner.increment(0);

            Doctor doctor = editingDoctor != null ? editingDoctor : new Doctor();
            doctor.setFullName(nameField.getText());
            doctor.setDepartment(departmentCombo.getValue() == null ? "" : departmentCombo.getValue());
            doctor.setSpecialization(specializationField.getText());
            doctor.setPhone(phoneField.getText());
            doctor.setEmail(emailField.getText());
            doctor.setConsultationFee(feeSpinner.getValue() == null ? 0 : feeSpinner.getValue());
            doctor.setWorkingSchedule(scheduleField.getText());

            if (editingDoctor != null) {
                doctorService.updateDoctor(doctor);
            } else {
                doctorService.addDoctor(doctor);
            }

            if (onSaved != null) {
                onSaved.accept(doctor);
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
