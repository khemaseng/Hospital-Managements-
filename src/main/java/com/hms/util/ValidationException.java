package com.hms.util;

/**
 * Thrown when user-supplied data fails validation (empty fields, bad
 * formats, duplicate IDs, out-of-range values, etc). Caught by controllers
 * and shown to the user as a JavaFX Alert rather than crashing the app.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
