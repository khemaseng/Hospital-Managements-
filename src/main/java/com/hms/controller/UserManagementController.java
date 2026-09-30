
package com.hms.controller;

import com.hms.model.User;
import com.hms.model.UserRole;
import com.hms.repository.UserRepository;
import com.hms.util.DialogUtil;
import com.hms.util.SessionManager;
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

public class UserManagementController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> sortFilter;
    @FXML private Label resultCountLabel;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, Void> colActions;

    private final UserRepository userRepository = new UserRepository();
    private final ObservableList<User> users = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        // Role Column with Centered Badge
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().name()));
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(role);
                badge.getStyleClass().addAll("badge", "badge-patient");
                setAlignment(Pos.CENTER);
                setGraphic(badge);
            }
        });

        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Status Column with Centered Badge
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(
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
                setAlignment(Pos.CENTER);
                setGraphic(badge);
            }
        });

        addActionButtons();

        // 1. Populate Roles
        roleFilter.getItems().add("All Roles");
        for (UserRole r : UserRole.values()) {
            roleFilter.getItems().add(r.name());
        }
        roleFilter.setValue("All Roles");

        // 2. Populate Status
        statusFilter.getItems().setAll("All Status", "Active", "Inactive");
        statusFilter.setValue("All Status");

        // 3. Populate Sort Options
        sortFilter.getItems().setAll(
                "Full Name (A-Z)",
                "Full Name (Z-A)",
                "Username (A-Z)",
                "Role"
        );
        sortFilter.setValue("Full Name (A-Z)");

        // 4. Attach Live Listeners for Instant Updates
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refresh());
        roleFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());
        sortFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());

        userTable.setItems(users);
        refresh();
    }

    private void refresh() {
        String keyword = searchField != null ? searchField.getText() : "";
        String role = roleFilter != null && roleFilter.getValue() != null ? roleFilter.getValue() : "All Roles";
        String status = statusFilter != null && statusFilter.getValue() != null ? statusFilter.getValue() : "All Status";
        String sortBy = sortFilter != null && sortFilter.getValue() != null ? sortFilter.getValue() : "Full Name (A-Z)";

        List<User> results = userRepository.search(keyword, role, status, sortBy);
        users.setAll(results);

        if (resultCountLabel != null) {
            resultCountLabel.setText(results.size() + " account(s)");
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
        DialogUtil.ModalHandle handle = DialogUtil.openModalDeferred("/fxml/UserFormDialog.fxml", "Add User");
        UserFormController controller = handle.controller();
        controller.setOnSaved(u -> refresh());
        handle.showAndWait();
    }

    private void handleToggleStatus(User user) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getId() == user.getId()) {
            DialogUtil.showError("Operation Blocked", "You cannot deactivate your own current login account.");
            return;
        }

        boolean willActivate = !user.isActive();
        String actionText = willActivate ? "Activate" : "Deactivate";
        boolean confirmed = DialogUtil.confirm(actionText + " Account",
                "Are you sure you want to " + actionText.toLowerCase() + " account '" + user.getUsername() + "'?");
        if (!confirmed) {
            return;
        }

        userRepository.updateStatus(user.getId(), willActivate);
        refresh();
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button toggleBtn = new Button();
            private final HBox box = new HBox(6, toggleBtn);

            {
                box.setAlignment(Pos.CENTER);
                toggleBtn.setOnAction(e -> handleToggleStatus(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                User u = getTableView().getItems().get(getIndex());
                if (u.isActive()) {
                    toggleBtn.setText("Deactivate");
                    toggleBtn.getStyleClass().setAll("btn-danger");
                    toggleBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                } else {
                    toggleBtn.setText("Activate");
                    toggleBtn.getStyleClass().setAll("btn-secondary");
                    toggleBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                }
                setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });
    }
}