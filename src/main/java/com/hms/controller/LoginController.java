package com.hms.controller;

import com.hms.model.User;
import com.hms.service.AuthService;
import com.hms.util.DialogUtil;
import com.hms.util.IconFactory;
import com.hms.util.ValidationException;
import com.hms.view.BlueprintBackgroundPainter;
import com.hms.view.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class LoginController {

    @FXML private Canvas backgroundCanvas;
    @FXML private StackPane logoContainer;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordButton;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();
    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
//        logoContainer.getChildren().add(IconFactory.gearCrossLogo(92, false));
//        togglePasswordButton.setGraphic(IconFactory.eye(13, "icon-shape-dark"));
//        bindBackgroundCanvasToParentSize();
    }

    /**
     * The background canvas has no fixed size in FXML - it needs to track
     * whatever size the root StackPane ends up at (which itself tracks the
     * window), and repaint whenever that changes, since Canvas content
     * doesn't automatically rescale like a resizable Node would.
     */
    private void bindBackgroundCanvasToParentSize() {
        backgroundCanvas.widthProperty().addListener((obs, oldVal, newVal) -> repaintBackground());
        backgroundCanvas.heightProperty().addListener((obs, oldVal, newVal) -> repaintBackground());

        // Defer binding until the canvas is actually attached to a parent with a real size.
        backgroundCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && backgroundCanvas.getParent() instanceof javafx.scene.layout.StackPane parent) {
                backgroundCanvas.widthProperty().bind(parent.widthProperty());
                backgroundCanvas.heightProperty().bind(parent.heightProperty());
            }
        });
    }

    private void repaintBackground() {
        BlueprintBackgroundPainter.paint(backgroundCanvas.getGraphicsContext2D(),
                backgroundCanvas.getWidth(), backgroundCanvas.getHeight());
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

    /**
     * Toggles between the masked PasswordField and a plain TextField showing
     * the same text. JavaFX's PasswordField can't reveal its own text, so
     * the classic approach is two overlapping fields with only one visible
     * at a time, kept in sync when swapping.
     */
    @FXML
    private void handleTogglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordVisibleField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            togglePasswordButton.setGraphic(IconFactory.eyeSlash(13, "icon-shape-dark"));
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordButton.setGraphic(IconFactory.eye(13, "icon-shape-dark"));
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
