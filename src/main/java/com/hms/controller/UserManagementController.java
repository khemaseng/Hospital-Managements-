package com.hms.controller;

import com.hms.model.User;
import com.hms.repository.UserRepository;
import com.hms.util.DialogUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class UserManagementController {

    @FXML private Label resultCountLabel;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colStatus;

    // Repository used directly here (no dedicated UserManagementService) since
    // this screen is read-mostly; account creation goes through AuthService.register
    // (see UserFormController), which owns the actual validation/hashing rules.
    private final UserRepository userRepository = new UserRepository();
    private final ObservableList<User> users = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRole().name()));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().isActive() ? "Active" : "Inactive"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().addAll("badge", status.equals("Active") ? "badge-completed" : "badge-cancelled");
                setGraphic(badge);
            }
        });

        userTable.setItems(users);
        refresh();
    }

    private void refresh() {
        List<User> results = userRepository.findAll();
        users.setAll(results);
        resultCountLabel.setText(results.size() + " account(s)");
    }

    @FXML
    private void handleAdd() {
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/UserFormDialog.fxml", "Add User");
        UserFormController controller = handle.controller();
        controller.setOnSaved(u -> refresh());
        handle.showAndWait();
    }
}
