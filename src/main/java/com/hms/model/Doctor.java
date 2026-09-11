package com.hms.model;

import javafx.beans.property.*;

public class Doctor {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty doctorCode = new SimpleStringProperty(this, "doctorCode");
    private final StringProperty fullName = new SimpleStringProperty(this, "fullName");
    private final StringProperty department = new SimpleStringProperty(this, "department");
    private final StringProperty specialization = new SimpleStringProperty(this, "specialization");
    private final StringProperty phone = new SimpleStringProperty(this, "phone");
    private final StringProperty email = new SimpleStringProperty(this, "email");
    private final StringProperty workingSchedule = new SimpleStringProperty(this, "workingSchedule");
    private final DoubleProperty consultationFee = new SimpleDoubleProperty(this, "consultationFee");

    public Doctor() {
    }

    public Doctor(int id, String doctorCode, String fullName, String department, String specialization,
                  String phone, String email, String workingSchedule, double consultationFee) {
        setId(id);
        setDoctorCode(doctorCode);
        setFullName(fullName);
        setDepartment(department);
        setSpecialization(specialization);
        setPhone(phone);
        setEmail(email);
        setWorkingSchedule(workingSchedule);
        setConsultationFee(consultationFee);
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getDoctorCode() { return doctorCode.get(); }
    public void setDoctorCode(String value) { doctorCode.set(value); }
    public StringProperty doctorCodeProperty() { return doctorCode; }

    public String getFullName() { return fullName.get(); }
    public void setFullName(String value) { fullName.set(value); }
    public StringProperty fullNameProperty() { return fullName; }

    public String getDepartment() { return department.get(); }
    public void setDepartment(String value) { department.set(value); }
    public StringProperty departmentProperty() { return department; }

    public String getSpecialization() { return specialization.get(); }
    public void setSpecialization(String value) { specialization.set(value); }
    public StringProperty specializationProperty() { return specialization; }

    public String getPhone() { return phone.get(); }
    public void setPhone(String value) { phone.set(value); }
    public StringProperty phoneProperty() { return phone; }

    public String getEmail() { return email.get(); }
    public void setEmail(String value) { email.set(value); }
    public StringProperty emailProperty() { return email; }

    public String getWorkingSchedule() { return workingSchedule.get(); }
    public void setWorkingSchedule(String value) { workingSchedule.set(value); }
    public StringProperty workingScheduleProperty() { return workingSchedule; }

    public double getConsultationFee() { return consultationFee.get(); }
    public void setConsultationFee(double value) { consultationFee.set(value); }
    public DoubleProperty consultationFeeProperty() { return consultationFee; }

    @Override
    public String toString() {
        return "Dr. " + getFullName() + " (" + getDepartment() + ")";
    }
}
