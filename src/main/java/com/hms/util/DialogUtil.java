package com.hms.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Small helpers so every controller shows errors/confirmations the same way
 * (JavaFX Alert dialogs) instead of letting exceptions bubble up and crash
 * the UI thread.
 */
public final class DialogUtil {

    private DialogUtil() {
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Loads an FXML file into a new modal Stage and returns its controller
     * so the caller can wire callbacks before showing it.
     */
    public static <T> T openModal(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(DialogUtil.class.getResource(fxmlPath)));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(DialogUtil.class.getResource("/css/application.css")).toExternalForm());
            stage.setScene(scene);
            stage.setResizable(false);
            T controller = loader.getController();
            stage.showAndWait();
            return controller;
        } catch (IOException e) {
            throw new DataAccessException("Failed to open dialog: " + fxmlPath, e);
        }
    }

    /**
     * Same as openModal but gives the caller the Stage too, for cases where
     * the controller needs to be configured (setPatientToEdit, etc.) before
     * the dialog is displayed and blocks.
     */
    public static ModalHandle openModalDeferred(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(DialogUtil.class.getResource(fxmlPath)));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(DialogUtil.class.getResource("/css/application.css")).toExternalForm());
            stage.setScene(scene);
            stage.setResizable(false);
            return new ModalHandle(stage, loader.getController());
        } catch (IOException e) {
            throw new DataAccessException("Failed to open dialog: " + fxmlPath, e);
        }
    }

    public static final class ModalHandle {
        public final Stage stage;
        public final Object controller;

        ModalHandle(Stage stage, Object controller) {
            this.stage = stage;
            this.controller = controller;
        }

        @SuppressWarnings("unchecked")
        public <T> T controller() {
            return (T) controller;
        }

        public void showAndWait() {
            stage.showAndWait();
        }
    }
}
