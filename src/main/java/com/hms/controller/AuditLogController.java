
package com.hms.controller;

import com.hms.model.AuditLog;
import com.hms.service.AuditService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuditLogController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> actionFilter;
    @FXML private ComboBox<String> sortFilter;
    @FXML private Label resultCountLabel;
    @FXML private TableView<AuditLog> logTable;
    @FXML private TableColumn<AuditLog, String> colTime;
    @FXML private TableColumn<AuditLog, String> colUser;
    @FXML private TableColumn<AuditLog, String> colAction;
    @FXML private TableColumn<AuditLog, String> colDetails;

    private final AuditService auditService = new AuditService();
    private final ObservableList<AuditLog> logs = FXCollections.observableArrayList();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

    @FXML
    public void initialize() {
        // Formatted Time Column (Centered)
        colTime.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getOccurredAt() != null ? data.getValue().getOccurredAt().format(TIME_FORMAT) : ""));
        colTime.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));

        // Action Column with Styled Badges (Centered)
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colAction.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String action, boolean empty) {
                super.updateItem(action, empty);
                if (empty || action == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(action);
                badge.getStyleClass().addAll("badge", badgeClass(action));
                setAlignment(Pos.CENTER);
                setGraphic(badge);
            }
        });

        colDetails.setCellValueFactory(new PropertyValueFactory<>("details"));

        // 1. Populate Action Filters
        actionFilter.getItems().setAll(
                "All Actions",
                "LOGIN",
                "LOGIN_FAILED",
                "USER_REGISTERED",
                "PATIENT_DELETED",
                "APPOINTMENT",
                "ROOM_ASSIGNED"
        );
        actionFilter.setValue("All Actions");

        // 2. Populate Sort Filters
        sortFilter.getItems().setAll(
                "Time (Newest)",
                "Time (Oldest)",
                "User (A-Z)",
                "Action (A-Z)"
        );
        sortFilter.setValue("Time (Newest)");

        // 3. Bind Live Property Listeners
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refresh());
        actionFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());
        sortFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());

        logTable.setItems(logs);
        refresh();
    }

    private String badgeClass(String action) {
        if (action == null) return "badge-scheduled";
        String act = action.toUpperCase();
        if (act.contains("FAIL") || act.contains("DELETE")) {
            return "badge-cancelled"; // Red
        } else if (act.contains("LOGIN") || act.contains("REGISTER")) {
            return "badge-completed"; // Green
        } else if (act.contains("UPDATE") || act.contains("ASSIGN")) {
            return "badge-noshow";    // Amber/Gold
        }
        return "badge-scheduled";     // Teal
    }

    private void refresh() {
        String keyword = searchField != null ? searchField.getText() : "";
        String action = actionFilter != null && actionFilter.getValue() != null ? actionFilter.getValue() : "All Actions";
        String sortBy = sortFilter != null && sortFilter.getValue() != null ? sortFilter.getValue() : "Time (Newest)";

        List<AuditLog> results = auditService.search(keyword, action, sortBy, 200);
        logs.setAll(results);

        if (resultCountLabel != null) {
            resultCountLabel.setText(results.size() + " log entry(s)");
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
    private void handleRefresh() {
        refresh();
    }
}