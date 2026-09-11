package com.hms.view;

import com.hms.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Owns the primary Stage and knows how to swap between the Login scene and
 * the main application shell (sidebar + topbar + content). Centralizing
 * this here means controllers never touch Stage directly.
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
            primaryStage.setTitle("Hospital Management System - " + user.getFullName());
            primaryStage.setMaximized(true);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load main dashboard.", e);
        }
    }

    public void toggleDarkMode() {
        darkMode = !darkMode;
        applyTheme(primaryStage.getScene());
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    private void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(APP_CSS)).toExternalForm());
        if (darkMode) {
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(DARK_CSS)).toExternalForm());
        }
    }

    private Parent load(String fxmlPath) throws IOException {
        return FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
