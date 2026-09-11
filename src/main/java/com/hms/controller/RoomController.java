package com.hms.controller;

import com.hms.model.Room;
import com.hms.service.RoomService;
import com.hms.util.DialogUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;

public class RoomController {

    @FXML private Label occupancySummaryLabel;
    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, String> colCode;
    @FXML private TableColumn<Room, String> colType;
    @FXML private TableColumn<Room, String> colFloor;
    @FXML private TableColumn<Room, String> colOccupancy;
    @FXML private TableColumn<Room, String> colNotes;
    @FXML private TableColumn<Room, Void> colActions;

    private final RoomService roomService = new RoomService();
    private final ObservableList<Room> rooms = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("roomCode"));
        colType.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRoomType().getDisplayName()));
        colFloor.setCellValueFactory(new PropertyValueFactory<>("floor"));
        colOccupancy.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getOccupantCount() + " / " + data.getValue().getCapacity()));
        colOccupancy.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                    return;
                }
                Room room = getTableView().getItems().get(getIndex());
                Label badge = new Label(value);
                badge.getStyleClass().addAll("badge", room.isFull() ? "badge-cancelled"
                        : room.getOccupantCount() > 0 ? "badge-scheduled" : "badge-completed");
                setGraphic(badge);
            }
        });
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        addActionButtons();

        roomTable.setItems(rooms);
        refresh();
    }

    private void refresh() {
        List<Room> results = roomService.getAllRooms();
        rooms.setAll(results);
        int totalBeds = results.stream().mapToInt(Room::getCapacity).sum();
        int occupied = results.stream().mapToInt(Room::getOccupantCount).sum();
        occupancySummaryLabel.setText(occupied + " / " + totalBeds + " beds occupied");
    }

    @FXML
    private void handleAdd() {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/RoomFormDialog.fxml", "Add Room");
        RoomFormController controller = handle.controller();
        controller.setOnSaved(r -> refresh());
        handle.showAndWait();
    }

    private void handleEdit(Room room) {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/RoomFormDialog.fxml", "Edit Room");
        RoomFormController controller = handle.controller();
        controller.setRoomToEdit(room);
        controller.setOnSaved(r -> refresh());
        handle.showAndWait();
    }

    private void handleDelete(Room room) {
        if (room.getOccupantCount() > 0) {
            DialogUtil.showError("Cannot Delete Room",
                    "Room " + room.getRoomCode() + " still has " + room.getOccupantCount() +
                            " patient(s) assigned. Discharge them first.");
            return;
        }
        boolean confirmed = DialogUtil.confirm("Delete Room", "Delete room " + room.getRoomCode() + "?");
        if (!confirmed) {
            return;
        }
        try {
            roomService.deleteRoom(room.getId());
            refresh();
        } catch (Exception e) {
            DialogUtil.showError("Delete Failed", "Could not delete this room.");
        }
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-secondary");
                deleteBtn.getStyleClass().add("btn-danger");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }
}
