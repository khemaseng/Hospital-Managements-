package com.hms.controller;

import com.hms.model.Invoice;
import com.hms.model.Patient;
import com.hms.service.BillingService;
import com.hms.service.PatientService;
import com.hms.util.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class InvoiceFormController {

    @FXML private Label formErrorLabel;
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private Spinner<Double> consultationSpinner;
    @FXML private Spinner<Double> medicineSpinner;
    @FXML private Spinner<Double> labSpinner;
    @FXML private Spinner<Double> discountSpinner;
    @FXML private Spinner<Double> taxSpinner;
    @FXML private Label totalLabel;
    @FXML private Button saveButton;

    private final PatientService patientService = new PatientService();
    private final BillingService billingService = new BillingService();
    private Consumer<Invoice> onSaved;

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

        consultationSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10000, 0, 5));
        medicineSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10000, 0, 5));
        labSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10000, 0, 5));
        discountSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10000, 0, 5));
        taxSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10000, 0, 1));

        for (Spinner<Double> s : List.of(consultationSpinner, medicineSpinner, labSpinner, discountSpinner, taxSpinner)) {
            s.valueProperty().addListener((obs, oldVal, newVal) -> recalculateTotal());
        }
        recalculateTotal();
    }

    public void setOnSaved(Consumer<Invoice> callback) {
        this.onSaved = callback;
    }

    private void recalculateTotal() {
        double subtotal = val(consultationSpinner) + val(medicineSpinner) + val(labSpinner) - val(discountSpinner);
        double total = Math.max(0, subtotal + val(taxSpinner));
        totalLabel.setText(String.format(Locale.US, "$%,.2f", total));
    }

    private double val(Spinner<Double> spinner) {
        return spinner.getValue() == null ? 0 : spinner.getValue();
    }

    @FXML
    private void handleSave() {
        hideError();
        try {
            Patient patient = patientCombo.getValue();
            if (patient == null) {
                throw new ValidationException("Please select a patient.");
            }
            Invoice invoice = new Invoice();
            invoice.setPatientId(patient.getId());
            invoice.setConsultationFee(val(consultationSpinner));
            invoice.setMedicineFee(val(medicineSpinner));
            invoice.setLabFee(val(labSpinner));
            invoice.setDiscount(val(discountSpinner));
            invoice.setTax(val(taxSpinner));

            Invoice saved = billingService.createInvoice(invoice);
            saved.setPatientName(patient.getFullName());

            if (onSaved != null) {
                onSaved.accept(saved);
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
