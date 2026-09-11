package com.hms.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Thin wrapper around jBCrypt so the rest of the codebase never touches
 * the hashing library directly (single point of change if the algorithm
 * is ever upgraded).
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    public static boolean verify(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Malformed hash in the database - treat as failed auth, never crash.
            return false;
        }
    }
}
