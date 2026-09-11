package com.hms.controller;

import com.hms.model.User;
import com.hms.service.AuthService;
import com.hms.util.DialogUtil;
import com.hms.util.ImageUtil;
import com.hms.util.SessionManager;
import com.hms.util.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ProfileController {

    @FXML private ImageView avatarPreview;
    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private Label avatarErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private final AuthService authService = new AuthService();
    private Runnable onAvatarChanged;

    @FXML
    public void initialize() {
        ImageUtil.makeCircular(avatarPreview, 36);
        User current = SessionManager.getInstance().getCurrentUser();
        if (current != null) {
            nameLabel.setText(current.getFullName());
            roleLabel.setText(current.getRole().name());
            loadAvatarPreview(current.getAvatarPath());
        }
    }

    /** Lets MainLayoutController refresh the top-bar avatar after a change here. */
    public void setOnAvatarChanged(Runnable callback) {
        this.onAvatarChanged = callback;
    }

    private void loadAvatarPreview(String path) {
        Image image = ImageUtil.loadAvatar(path);
        if (image != null) {
            avatarPreview.setImage(image);
        }
    }

    @FXML
    private void handleChoosePhoto() {
        hideAvatarError();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Profile Picture");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        Stage stage = (Stage) avatarPreview.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        try {
            authService.updateAvatar(file.toPath());
            loadAvatarPreview(SessionManager.getInstance().getCurrentUser().getAvatarPath());
            if (onAvatarChanged != null) {
                onAvatarChanged.run();
            }
        } catch (ValidationException e) {
            showAvatarError(e.getMessage());
        } catch (Exception e) {
            showAvatarError("Could not update your photo: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePassword() {
        hidePasswordError();
        String current = currentPasswordField.getText();
        String next = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (next == null || !next.equals(confirm)) {
            showPasswordError("New password and confirmation do not match.");
            return;
        }
        try {
            authService.changePassword(current, next);
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            passwordErrorLabel.setVisible(false);
            passwordErrorLabel.setManaged(false);
            DialogUtil.showInfo("Password Updated", "Your password has been changed successfully.");
        } catch (ValidationException e) {
            showPasswordError(e.getMessage());
        } catch (Exception e) {
            showPasswordError("Could not change your password: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) nameLabel.getScene().getWindow()).close();
    }

    private void showAvatarError(String message) {
        avatarErrorLabel.setText(message);
        avatarErrorLabel.setVisible(true);
        avatarErrorLabel.setManaged(true);
    }

    private void hideAvatarError() {
        avatarErrorLabel.setVisible(false);
        avatarErrorLabel.setManaged(false);
    }

    private void showPasswordError(String message) {
        passwordErrorLabel.setText(message);
        passwordErrorLabel.setVisible(true);
        passwordErrorLabel.setManaged(true);
    }

    private void hidePasswordError() {
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setManaged(false);
    }
}
