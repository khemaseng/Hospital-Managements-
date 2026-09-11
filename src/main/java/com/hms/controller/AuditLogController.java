package com.hms.controller;

import com.hms.model.AuditLog;
import com.hms.service.AuditService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;

public class AuditLogController {

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
        colTime.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getOccurredAt() != null ? data.getValue().getOccurredAt().format(TIME_FORMAT) : ""));
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colDetails.setCellValueFactory(new PropertyValueFactory<>("details"));

        logTable.setItems(logs);
        refresh();
    }

    @FXML
    private void handleRefresh() {
        refresh();
    }

    private void refresh() {
        logs.setAll(auditService.getRecent(200));
    }
}
