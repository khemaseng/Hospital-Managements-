package com.hms.controller;

import com.hms.model.Room;
import com.hms.model.RoomType;
import com.hms.service.RoomService;
import com.hms.util.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class RoomFormController {

    @FXML private Label dialogTitleLabel;
    @FXML private Label formErrorLabel;
    @FXML private TextField roomCodeField;
    @FXML private ComboBox<RoomType> roomTypeCombo;
    @FXML private Spinner<Integer> capacitySpinner;
    @FXML private TextField floorField;
    @FXML private TextField notesField;
    @FXML private Button saveButton;

    private final RoomService roomService = new RoomService();
    private Room editingRoom; // null => add mode
    private Consumer<Room> onSaved;

    @FXML
    public void initialize() {
        roomTypeCombo.getItems().addAll(RoomType.values());
        capacitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
    }

    public void setOnSaved(Consumer<Room> callback) {
        this.onSaved = callback;
    }

    public void setRoomToEdit(Room room) {
        this.editingRoom = room;
        dialogTitleLabel.setText("Edit Room - " + room.getRoomCode());
        roomCodeField.setText(room.getRoomCode());
        roomCodeField.setDisable(true); // room code is immutable once created
        roomTypeCombo.setValue(room.getRoomType());
        capacitySpinner.getValueFactory().setValue(room.getCapacity());
        floorField.setText(room.getFloor());
        notesField.setText(room.getNotes());
        saveButton.setText("Update Room");
    }

    @FXML
    private void handleSave() {
        hideError();
        try {
            capacitySpinner.increment(0); // commit any in-progress text edit
            Room room = editingRoom != null ? editingRoom : new Room();
            room.setRoomCode(roomCodeField.getText());
            room.setRoomType(roomTypeCombo.getValue());
            room.setCapacity(capacitySpinner.getValue());
            room.setFloor(floorField.getText());
            room.setNotes(notesField.getText());

            if (editingRoom != null) {
                roomService.updateRoom(room);
            } else {
                roomService.addRoom(room);
            }

            if (onSaved != null) {
                onSaved.accept(room);
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
