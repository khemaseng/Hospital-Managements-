package com.hms.controller;

import com.hms.service.AuthService;
import com.hms.util.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ForgotPasswordController {

    @FXML private TextField usernameField;
    @FXML private Label errorLabel;
    @FXML private Label resultLabel;
    @FXML private Button resetButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleReset() {
        hideMessages();
        try {
            String tempPassword = authService.resetPasswordByUsername(usernameField.getText());
            resultLabel.setText("Your temporary password is: " + tempPassword
                    + "\nSign in with it, then change your password from My Profile.");
            resultLabel.setVisible(true);
            resultLabel.setManaged(true);
            resetButton.setDisable(true);
            usernameField.setDisable(true);
        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Something went wrong. Please try again.");
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) usernameField.getScene().getWindow()).close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideMessages() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        resultLabel.setVisible(false);
        resultLabel.setManaged(false);
    }
}
