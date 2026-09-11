package com.hms.controller;

import com.hms.model.User;
import com.hms.model.UserRole;
import com.hms.service.AuthService;
import com.hms.util.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class UserFormController {

    @FXML private Label formErrorLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private ComboBox<UserRole> roleCombo;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button saveButton;

    private final AuthService authService = new AuthService();
    private Consumer<User> onSaved;

    @FXML
    public void initialize() {
        roleCombo.getItems().addAll(UserRole.values());
        roleCombo.setValue(UserRole.RECEPTIONIST);
    }

    public void setOnSaved(Consumer<User> callback) {
        this.onSaved = callback;
    }

    @FXML
    private void handleSave() {
        hideError();
        try {
            User user = authService.register(
                    usernameField.getText(),
                    passwordField.getText(),
                    roleCombo.getValue(),
                    fullNameField.getText(),
                    emailField.getText()
            );
            if (onSaved != null) {
                onSaved.accept(user);
            }
            closeWindow();
        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("An unexpected error occurred while creating the account: " + e.getMessage());
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
