
package com.hms.controller;

import com.hms.model.User;
import com.hms.service.AuthService;
import com.hms.util.DialogUtil;
import com.hms.util.IconFactory;
import com.hms.util.ValidationException;
import com.hms.view.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordButton;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();
    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        // Starts with the slashed eye icon because password input is hidden by default
        if (togglePasswordButton != null) {
            togglePasswordButton.setGraphic(IconFactory.eyeSlash(17, "icon-shape-dark"));
        }

        // Two-way sync between hidden password field and visible text field
        if (passwordField != null && passwordVisibleField != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!passwordVisibleField.getText().equals(newVal)) {
                    passwordVisibleField.setText(newVal);
                }
            });
            passwordVisibleField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!passwordField.getText().equals(newVal)) {
                    passwordField.setText(newVal);
                }
            });
        }
    }

    @FXML
    private void handleLogin() {
        hideError();
        try {
            String password = passwordVisible ? passwordVisibleField.getText() : passwordField.getText();
            User user = authService.login(usernameField.getText(), password);
            SceneManager.getInstance().showDashboard(user);
        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Unable to sign in right now. Please try again.");
        }
    }

    @FXML
    private void handleTogglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordVisibleField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordVisibleField.requestFocus();
            passwordVisibleField.positionCaret(passwordVisibleField.getText().length());
            // Swaps to regular eye icon when password text is displayed in plain text
            togglePasswordButton.setGraphic(IconFactory.eye(17, "icon-shape-dark"));
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            // Swaps to slashed eye icon when password text is masked again
            togglePasswordButton.setGraphic(IconFactory.eyeSlash(17, "icon-shape-dark"));
        }
    }

    @FXML
    private void handleForgotPassword() {
        DialogUtil.openModal("/fxml/ForgotPasswordDialog.fxml", "Reset Password");
    }

    @FXML
    private void handleCreateAccount() {
        DialogUtil.openModal("/fxml/RegisterDialog.fxml", "Create an Account");
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