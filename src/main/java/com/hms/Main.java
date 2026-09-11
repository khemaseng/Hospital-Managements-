package com.hms;

import com.hms.database.DatabaseConnection;
import com.hms.view.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Application entry point. Initializes the database (creating the schema
 * and seed data on first run) and shows the login screen.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseConnection.initializeDatabase();

        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.init(primaryStage);
        sceneManager.showLogin();

        primaryStage.show();
    }

    @Override
    public void stop() {
        DatabaseConnection.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
