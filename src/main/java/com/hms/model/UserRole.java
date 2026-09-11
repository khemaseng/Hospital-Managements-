package com.hms.model;

/**
 * The three roles supported by the system. Each role maps to a distinct
 * permission set enforced in the service layer (see AuthService /
 * SessionManager) and reflected in the UI (sidebar items are filtered by role).
 */
public enum UserRole {
    ADMIN,
    DOCTOR,
    RECEPTIONIST
}
