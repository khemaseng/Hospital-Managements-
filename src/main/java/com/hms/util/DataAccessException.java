package com.hms.util;

/**
 * Wraps low-level SQLException into an unchecked exception carrying a
 * user-friendly message, so repository callers don't need to handle raw
 * SQLException at every call site.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
