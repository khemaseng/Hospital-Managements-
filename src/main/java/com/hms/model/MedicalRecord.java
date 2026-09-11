package com.hms.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class MedicalRecord {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty appointmentId = new SimpleIntegerProperty(this, "appointmentId");
    private final IntegerProperty patientId = new SimpleIntegerProperty(this, "patientId");
    private final StringProperty patientName = new SimpleStringProperty(this, "patientName");
    private final IntegerProperty doctorId = new SimpleIntegerProperty(this, "doctorId");
    private final StringProperty doctorName = new SimpleStringProperty(this, "doctorName");
    private final StringProperty diagnosis = new SimpleStringProperty(this, "diagnosis");
    private final StringProperty symptoms = new SimpleStringProperty(this, "symptoms");
    private final StringProperty prescription = new SimpleStringProperty(this, "prescription");
    private final StringProperty labResult = new SimpleStringProperty(this, "labResult");
    private final StringProperty doctorNotes = new SimpleStringProperty(this, "doctorNotes");
    private final ObjectProperty<LocalDate> recordDate = new SimpleObjectProperty<>(this, "recordDate");

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getAppointmentId() { return appointmentId.get(); }
    public void setAppointmentId(int value) { appointmentId.set(value); }
    public IntegerProperty appointmentIdProperty() { return appointmentId; }

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

    public String getDiagnosis() { return diagnosis.get(); }
    public void setDiagnosis(String value) { diagnosis.set(value); }
    public StringProperty diagnosisProperty() { return diagnosis; }

    public String getSymptoms() { return symptoms.get(); }
    public void setSymptoms(String value) { symptoms.set(value); }
    public StringProperty symptomsProperty() { return symptoms; }

    public String getPrescription() { return prescription.get(); }
    public void setPrescription(String value) { prescription.set(value); }
    public StringProperty prescriptionProperty() { return prescription; }

    public String getLabResult() { return labResult.get(); }
    public void setLabResult(String value) { labResult.set(value); }
    public StringProperty labResultProperty() { return labResult; }

    public String getDoctorNotes() { return doctorNotes.get(); }
    public void setDoctorNotes(String value) { doctorNotes.set(value); }
    public StringProperty doctorNotesProperty() { return doctorNotes; }

    public LocalDate getRecordDate() { return recordDate.get(); }
    public void setRecordDate(LocalDate value) { recordDate.set(value); }
    public ObjectProperty<LocalDate> recordDateProperty() { return recordDate; }
}
