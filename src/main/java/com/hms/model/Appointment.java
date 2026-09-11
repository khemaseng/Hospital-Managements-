package com.hms.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a booked appointment slot linking a Patient to a Doctor.
 * Conflict prevention (same doctor + overlapping time) is enforced in
 * AppointmentService, not here — this class is a plain data holder.
 */
public class Appointment {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty appointmentCode = new SimpleStringProperty(this, "appointmentCode");
    private final IntegerProperty patientId = new SimpleIntegerProperty(this, "patientId");
    private final StringProperty patientName = new SimpleStringProperty(this, "patientName");
    private final IntegerProperty doctorId = new SimpleIntegerProperty(this, "doctorId");
    private final StringProperty doctorName = new SimpleStringProperty(this, "doctorName");
    private final StringProperty department = new SimpleStringProperty(this, "department");
    private final ObjectProperty<LocalDate> appointmentDate = new SimpleObjectProperty<>(this, "appointmentDate");
    private final ObjectProperty<LocalTime> appointmentTime = new SimpleObjectProperty<>(this, "appointmentTime");
    private final ObjectProperty<AppointmentStatus> status = new SimpleObjectProperty<>(this, "status");
    private final StringProperty reason = new SimpleStringProperty(this, "reason");

    public Appointment() {
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getAppointmentCode() { return appointmentCode.get(); }
    public void setAppointmentCode(String value) { appointmentCode.set(value); }
    public StringProperty appointmentCodeProperty() { return appointmentCode; }

    public int getPatientId() { return patientId.get(); }
    public void setPatientId(int value) { patientId.set(value); }
    public IntegerProperty patientIdProperty() { return patientId; }

    public String getPatientName() { return patientName.get(); }
    public void setPatientName(String value) { patientName.set(value); }
    public StringProperty patientNameProperty() { return patientName; }

    public int getDoctorId() { return doctorId.get(); }
    public void setDoctorId(int value) { doctorId.set(value); }
    public IntegerProperty doctorIdProperty() { return doctorId; }

    public String getDoctorName() { return doctorName.get(); }
    public void setDoctorName(String value) { doctorName.set(value); }
    public StringProperty doctorNameProperty() { return doctorName; }

    public String getDepartment() { return department.get(); }
    public void setDepartment(String value) { department.set(value); }
    public StringProperty departmentProperty() { return department; }

    public LocalDate getAppointmentDate() { return appointmentDate.get(); }
    public void setAppointmentDate(LocalDate value) { appointmentDate.set(value); }
    public ObjectProperty<LocalDate> appointmentDateProperty() { return appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime.get(); }
    public void setAppointmentTime(LocalTime value) { appointmentTime.set(value); }
    public ObjectProperty<LocalTime> appointmentTimeProperty() { return appointmentTime; }

    public AppointmentStatus getStatus() { return status.get(); }
    public void setStatus(AppointmentStatus value) { status.set(value); }
    public ObjectProperty<AppointmentStatus> statusProperty() { return status; }

    public String getReason() { return reason.get(); }
    public void setReason(String value) { reason.set(value); }
    public StringProperty reasonProperty() { return reason; }
}
