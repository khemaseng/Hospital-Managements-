package com.hms.util;

import com.hms.model.User;
import com.hms.model.UserRole;

import java.time.LocalDateTime;

/**
 * Holds the currently authenticated user for the lifetime of the JavaFX
 * application. A desktop app has exactly one active session at a time, so a
 * simple singleton (rather than a servlet-style session store) is the right
 * amount of complexity here.
 */
public final class SessionManager {

    private static SessionManager instance;

    private User currentUser;
    private LocalDateTime loginTime;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
        this.loginTime = LocalDateTime.now();
    }

    public void logout() {
        this.currentUser = null;
        this.loginTime = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * Role-based authorization check. Admin has full access; Doctor and
     * Receptionist are restricted per module.
     */
    public boolean hasPermission(String module) {
        if (currentUser == null) {
            return false;
        }
        UserRole role = currentUser.getRole();
        if (role == UserRole.ADMIN) {
            return true; // Admin can access everything.
        }
        return switch (module) {
            case "PATIENTS", "APPOINTMENTS", "DASHBOARD" -> true; // all roles
            case "DOCTORS" -> role == UserRole.DOCTOR; // doctors manage their own profile only (handled in service)
            case "BILLING" -> role == UserRole.RECEPTIONIST;
            case "MEDICAL_RECORDS" -> role == UserRole.DOCTOR;
            case "USER_MANAGEMENT", "REPORTS" -> false; // admin-only
            default -> false;
        };
    }
}
