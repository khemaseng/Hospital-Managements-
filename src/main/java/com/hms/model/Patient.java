package com.hms.model;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Domain model for a Patient. Uses JavaFX properties so instances can be
 * bound directly to a TableView without a separate "view model" wrapper.
 */
public class Patient {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty patientCode = new SimpleStringProperty(this, "patientCode");
    private final StringProperty fullName = new SimpleStringProperty(this, "fullName");
    private final StringProperty gender = new SimpleStringProperty(this, "gender");
    private final IntegerProperty age = new SimpleIntegerProperty(this, "age");
    private final ObjectProperty<LocalDate> dateOfBirth = new SimpleObjectProperty<>(this, "dateOfBirth");
    private final StringProperty phone = new SimpleStringProperty(this, "phone");
    private final StringProperty email = new SimpleStringProperty(this, "email");
    private final StringProperty address = new SimpleStringProperty(this, "address");
    private final StringProperty bloodGroup = new SimpleStringProperty(this, "bloodGroup");
    private final StringProperty emergencyContact = new SimpleStringProperty(this, "emergencyContact");
    private final ObjectProperty<LocalDate> registeredOn = new SimpleObjectProperty<>(this, "registeredOn");
    private final ObjectProperty<Integer> roomId = new SimpleObjectProperty<>(this, "roomId");
    private final StringProperty roomCode = new SimpleStringProperty(this, "roomCode");

    public Patient() {
    }

    public Patient(int id, String patientCode, String fullName, String gender, int age,
                    LocalDate dateOfBirth, String phone, String email, String address,
                    String bloodGroup, String emergencyContact) {
        setId(id);
        setPatientCode(patientCode);
        setFullName(fullName);
        setGender(gender);
        setAge(age);
        setDateOfBirth(dateOfBirth);
        setPhone(phone);
        setEmail(email);
        setAddress(address);
        setBloodGroup(bloodGroup);
        setEmergencyContact(emergencyContact);
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getPatientCode() { return patientCode.get(); }
    public void setPatientCode(String value) { patientCode.set(value); }
    public StringProperty patientCodeProperty() { return patientCode; }

    public String getFullName() { return fullName.get(); }
    public void setFullName(String value) { fullName.set(value); }
    public StringProperty fullNameProperty() { return fullName; }

    public String getGender() { return gender.get(); }
    public void setGender(String value) { gender.set(value); }
    public StringProperty genderProperty() { return gender; }

    public int getAge() { return age.get(); }
    public void setAge(int value) { age.set(value); }
    public IntegerProperty ageProperty() { return age; }

    public LocalDate getDateOfBirth() { return dateOfBirth.get(); }
    public void setDateOfBirth(LocalDate value) { dateOfBirth.set(value); }
    public ObjectProperty<LocalDate> dateOfBirthProperty() { return dateOfBirth; }

    public String getPhone() { return phone.get(); }
    public void setPhone(String value) { phone.set(value); }
    public StringProperty phoneProperty() { return phone; }

    public String getEmail() { return email.get(); }
    public void setEmail(String value) { email.set(value); }
    public StringProperty emailProperty() { return email; }

    public String getAddress() { return address.get(); }
    public void setAddress(String value) { address.set(value); }
    public StringProperty addressProperty() { return address; }

    public String getBloodGroup() { return bloodGroup.get(); }
    public void setBloodGroup(String value) { bloodGroup.set(value); }
    public StringProperty bloodGroupProperty() { return bloodGroup; }

    public String getEmergencyContact() { return emergencyContact.get(); }
    public void setEmergencyContact(String value) { emergencyContact.set(value); }
    public StringProperty emergencyContactProperty() { return emergencyContact; }

    public LocalDate getRegisteredOn() { return registeredOn.get(); }
    public void setRegisteredOn(LocalDate value) { registeredOn.set(value); }
    public ObjectProperty<LocalDate> registeredOnProperty() { return registeredOn; }

    public Integer getRoomId() { return roomId.get(); }
    public void setRoomId(Integer value) { roomId.set(value); }
    public ObjectProperty<Integer> roomIdProperty() { return roomId; }

    public String getRoomCode() { return roomCode.get(); }
    public void setRoomCode(String value) { roomCode.set(value); }
    public StringProperty roomCodeProperty() { return roomCode; }

    public boolean isAdmitted() {
        return getRoomId() != null;
    }
}
