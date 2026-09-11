package com.hms.controller;

import com.hms.model.Patient;
import com.hms.model.Room;
import com.hms.service.RoomService;
import com.hms.util.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Consumer;

public class RoomAssignController {

    @FXML private Label titleLabel;
    @FXML private Label formErrorLabel;
    @FXML private Label currentStatusLabel;
    @FXML private ComboBox<Room> roomCombo;
    @FXML private Button dischargeButton;
    @FXML private Button admitButton;

    private final RoomService roomService = new RoomService();
    private Patient patient;
    private Runnable onChanged;

    public void setPatient(Patient patient) {
        this.patient = patient;
        titleLabel.setText("Room Assignment - " + patient.getFullName());
        refreshState();
    }

    public void setOnChanged(Runnable callback) {
        this.onChanged = callback;
    }

    @FXML
    public void initialize() {
        roomCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Room r) {
                return r == null ? "" : r.getRoomCode() + " - " + r.getRoomType().getDisplayName()
                        + " (" + r.getAvailableBeds() + " bed(s) free)";
            }

            @Override
            public Room fromString(String string) {
                return null;
            }
        });
    }

    private void refreshState() {
        hideError();
        List<Room> available = roomService.getAvailableRooms();
        roomCombo.getItems().setAll(available);

        boolean admitted = patient.isAdmitted();
        currentStatusLabel.setText(admitted ? "Admitted - Room " + patient.getRoomCode() : "Not admitted");
        dischargeButton.setDisable(!admitted);
        admitButton.setDisable(available.isEmpty());
        roomCombo.setDisable(available.isEmpty());
    }

    @FXML
    private void handleAdmit() {
        hideError();
        Room selected = roomCombo.getValue();
        if (selected == null) {
            showError("Please select a room.");
            return;
        }
        try {
            roomService.admitPatient(patient.getId(), selected.getId());
            patient.setRoomId(selected.getId());
            patient.setRoomCode(selected.getRoomCode());
            if (onChanged != null) {
                onChanged.run();
            }
            refreshState();
        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Could not admit patient: " + e.getMessage());
        }
    }

    @FXML
    private void handleDischarge() {
        try {
            roomService.dischargePatient(patient.getId());
            patient.setRoomId(null);
            patient.setRoomCode(null);
            if (onChanged != null) {
                onChanged.run();
            }
            refreshState();
        } catch (Exception e) {
            showError("Could not discharge patient: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) currentStatusLabel.getScene().getWindow()).close();
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
