
package com.hms.view;

import com.hms.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Owns the primary Stage and handles screen swapping (Login vs Main App Shell)
 * as well as global light/dark theme switching across all views and dialogs.
 */
public final class SceneManager {

    private static SceneManager instance;

    private Stage primaryStage;
    private boolean darkMode = false;

    private static final String APP_CSS = "/css/application.css";
    private static final String DARK_CSS = "/css/dark-theme.css";

    private SceneManager() {
    }

    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    public void showLogin() {
        try {
            Parent root = load("/fxml/Login.fxml");
            Scene scene = new Scene(root, 1000, 650);
            applyTheme(scene);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Hospital Management System - Sign In");
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load login screen.", e);
        }
    }

    public void showDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/fxml/MainLayout.fxml")));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 800);
            applyTheme(scene);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Hospital Management System - " + (user != null ? user.getFullName() : "Admin"));
            primaryStage.setMaximized(true);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load main dashboard.", e);
        }
    }

    /**
     * Toggles between Light and Dark mode on the primary stage.
     */
    public void toggleDarkMode() {
        darkMode = !darkMode;
        if (primaryStage != null && primaryStage.getScene() != null) {
            applyTheme(primaryStage.getScene());
        }
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    /**
     * Public method to allow modal dialogs and alerts (e.g., in DialogUtil)
     * to inherit the current theme.
     */
    public void applyCurrentTheme(Scene scene) {
        if (scene != null) {
            applyTheme(scene);
        }
    }

    private void applyTheme(Scene scene) {
        if (scene == null) return;

        scene.getStylesheets().clear();

        URL appCssUrl = getClass().getResource(APP_CSS);
        if (appCssUrl != null) {
            scene.getStylesheets().add(appCssUrl.toExternalForm());
        }

        if (darkMode) {
            URL darkCssUrl = getClass().getResource(DARK_CSS);
            if (darkCssUrl != null) {
                scene.getStylesheets().add(darkCssUrl.toExternalForm());
            }
        }
    }

    private Parent load(String fxmlPath) throws IOException {
        return FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}