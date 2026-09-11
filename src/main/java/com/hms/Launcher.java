package com.hms;

/**
 * A separate entry point that does NOT extend javafx.application.Application.
 *
 * When JavaFX is on the plain classpath (not the module path) and a jar's
 * main class directly extends Application, the java launcher refuses to
 * start with "JavaFX runtime components are missing" even though the
 * classes are present. Routing through this indirection avoids that check.
 * This is the class referenced by the runnable/shaded jar's manifest.
 */
public final class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
