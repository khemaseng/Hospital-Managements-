
package com.hms.controller;

import com.hms.model.Room;
import com.hms.model.RoomType;
import com.hms.service.RoomService;
import com.hms.util.DialogUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.util.List;

public class RoomController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> availabilityFilter;
    @FXML private ComboBox<String> sortFilter;
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
        colType.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRoomType() != null ? data.getValue().getRoomType().getDisplayName() : ""));
        colFloor.setCellValueFactory(new PropertyValueFactory<>("floor"));

        // Occupancy Column Centered with Badge
        colOccupancy.setCellValueFactory(data -> new SimpleStringProperty(
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
                setAlignment(Pos.CENTER);
                setGraphic(badge);
            }
        });

        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        addActionButtons();

        // 1. Populate Room Type Filter
        typeFilter.getItems().add("All Types");
        for (RoomType rt : RoomType.values()) {
            typeFilter.getItems().add(rt.getDisplayName());
        }
        typeFilter.setValue("All Types");

        // 2. Populate Availability Filter
        availabilityFilter.getItems().setAll("All Status", "Available (Has Space)", "Full (100% Occupied)");
        availabilityFilter.setValue("All Status");

        // 3. Populate Sort Options
        sortFilter.getItems().setAll(
                "Room Code (A-Z)",
                "Room Code (Z-A)",
                "Most Available",
                "Highest Occupancy",
                "Floor"
        );
        sortFilter.setValue("Room Code (A-Z)");

        // 4. Bind Listeners for Reactive Instant Updates
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refresh());
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());
        availabilityFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());
        sortFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());

        roomTable.setItems(rooms);
        refresh();
    }

    private void refresh() {
        String keyword = searchField != null ? searchField.getText() : "";
        String type = typeFilter != null && typeFilter.getValue() != null ? typeFilter.getValue() : "All Types";
        String avail = availabilityFilter != null && availabilityFilter.getValue() != null ? availabilityFilter.getValue() : "All Status";
        String sortBy = sortFilter != null && sortFilter.getValue() != null ? sortFilter.getValue() : "Room Code (A-Z)";

        List<Room> results = roomService.search(keyword, type, avail, sortBy);
        rooms.setAll(results);

        // Compute total live hospital beds stats
        List<Room> allRooms = roomService.getAllRooms();
        int totalBeds = allRooms.stream().mapToInt(Room::getCapacity).sum();
        int occupied = allRooms.stream().mapToInt(Room::getOccupantCount).sum();
        if (occupancySummaryLabel != null) {
            occupancySummaryLabel.setText(occupied + " / " + totalBeds + " beds occupied (" + results.size() + " rooms listed)");
        }
    }

    @FXML
    private void handleFilterChanged(KeyEvent event) {
        refresh();
    }

    @FXML
    private void handleFilterChanged() {
        refresh();
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
                box.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("btn-secondary");
                deleteBtn.getStyleClass().add("btn-danger");
                editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10 4 10;");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10 4 10;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }
}