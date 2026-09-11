package com.hms.controller;

import com.hms.model.Appointment;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.service.AppointmentService;
import com.hms.service.DoctorService;
import com.hms.service.PatientService;
import com.hms.util.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Booking form for a new appointment. Time slots are generated in 30-minute
 * increments (09:00-17:00); slots already taken for the selected doctor are
 * not filtered out here (the service layer performs the authoritative
 * conflict check), but this keeps the happy path simple to use.
 */
public class AppointmentFormController {

    @FXML private Label formErrorLabel;
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private Label departmentValueLabel;
    @FXML private Label feeValueLabel;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> timeCombo;
    @FXML private TextArea reasonField;
    @FXML private Button saveButton;

    private final PatientService patientService = new PatientService();
    private final DoctorService doctorService = new DoctorService();
    private final AppointmentService appointmentService = new AppointmentService();

    private Consumer<Appointment> onSaved;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

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

        for (LocalTime t = LocalTime.of(9, 0); !t.isAfter(LocalTime.of(17, 0)); t = t.plusMinutes(30)) {
            timeCombo.getItems().add(t);
        }
        timeCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(LocalTime t) {
                return t == null ? "" : t.format(TIME_FORMAT);
            }

            @Override
            public LocalTime fromString(String string) {
                return null;
            }
        });

        datePicker.setValue(java.time.LocalDate.now());
    }

    public void setOnSaved(Consumer<Appointment> callback) {
        this.onSaved = callback;
    }

    @FXML
    private void handleDoctorChanged() {
        Doctor doctor = doctorCombo.getValue();
        if (doctor != null) {
            departmentValueLabel.setText(doctor.getDepartment());
            feeValueLabel.setText(String.format(Locale.US, "$%,.2f", doctor.getConsultationFee()));
        } else {
            departmentValueLabel.setText("-");
            feeValueLabel.setText("-");
        }
    }

    @FXML
    private void handleSave() {
        hideError();
        try {
            Patient patient = patientCombo.getValue();
            Doctor doctor = doctorCombo.getValue();
            if (patient == null) {
                throw new ValidationException("Please select a patient.");
            }
            if (doctor == null) {
                throw new ValidationException("Please select a doctor.");
            }
            if (timeCombo.getValue() == null) {
                throw new ValidationException("Please select an appointment time.");
            }

            Appointment appointment = new Appointment();
            appointment.setPatientId(patient.getId());
            appointment.setDoctorId(doctor.getId());
            appointment.setDepartment(doctor.getDepartment());
            appointment.setAppointmentDate(datePicker.getValue());
            appointment.setAppointmentTime(timeCombo.getValue());
            appointment.setReason(reasonField.getText());

            Appointment saved = appointmentService.bookAppointment(appointment);
            saved.setPatientName(patient.getFullName());
            saved.setDoctorName(doctor.getFullName());

            if (onSaved != null) {
                onSaved.accept(saved);
            }
            closeWindow();
        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("An unexpected error occurred while booking: " + e.getMessage());
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
