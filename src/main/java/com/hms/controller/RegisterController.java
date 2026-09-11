package com.hms.controller;

import com.hms.model.UserRole;
import com.hms.service.AuthService;
import com.hms.util.DialogUtil;
import com.hms.util.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private Label errorLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button createButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleCreate() {
        hideError();
        if (passwordField.getText() == null || !passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Password and confirmation do not match.");
            return;
        }
        try {
            // Self-registration is intentionally limited to the least-privileged
            // role. Doctor/Admin accounts must be created by an administrator
            // via the User Accounts screen (see UserFormController).
            authService.register(usernameField.getText(), passwordField.getText(), UserRole.RECEPTIONIST,
                    fullNameField.getText(), emailField.getText());
            DialogUtil.showInfo("Account Created", "Your account has been created. You can now sign in.");
            closeWindow();
        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Could not create your account: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) createButton.getScene().getWindow()).close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
