package com.hms.service;

import com.hms.model.User;
import com.hms.model.UserRole;
import com.hms.repository.UserRepository;
import com.hms.util.ImageUtil;
import com.hms.util.PasswordUtil;
import com.hms.util.SessionManager;
import com.hms.util.ValidationException;
import com.hms.util.ValidationUtil;

import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * Handles authentication: login, logout, account creation, profile
 * pictures, and password changes. Wrong passwords never reveal whether the
 * username exists (generic error message) to avoid username enumeration.
 */
public class AuthService {

    private final UserRepository userRepository = new UserRepository();
    private final AuditService auditService = new AuditService();

    /**
     * Attempts to authenticate. Returns the User on success or throws
     * ValidationException with a message safe to show in a dialog.
     */
    public User login(String username, String password) throws ValidationException {
        ValidationUtil.requireNonEmpty(username, "Username");
        ValidationUtil.requireNonEmpty(password, "Password");

        Optional<User> found = userRepository.findByUsername(username.trim());
        if (found.isEmpty() || !found.get().isActive()) {
            auditService.log(username, "LOGIN_FAILED", "Unknown username or inactive account");
            throw new ValidationException("Invalid username or password.");
        }
        User user = found.get();
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            auditService.log(username, "LOGIN_FAILED", "Incorrect password");
            throw new ValidationException("Invalid username or password.");
        }
        userRepository.updateLastLogin(user.getId());
        SessionManager.getInstance().login(user);
        auditService.log("LOGIN", "User signed in");
        return user;
    }

    public void logout() {
        auditService.log("LOGOUT", "User signed out");
        SessionManager.getInstance().logout();
    }

    public User register(String username, String password, UserRole role, String fullName, String email)
            throws ValidationException {
        ValidationUtil.requireNonEmpty(username, "Username");
        ValidationUtil.requireNonEmpty(fullName, "Full name");
        ValidationUtil.validateEmail(email);
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters.");
        }
        if (userRepository.existsByUsername(username.trim())) {
            throw new ValidationException("Username '" + username + "' is already taken.");
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setRole(role);
        user.setFullName(fullName.trim());
        user.setEmail(email.trim());
        user.setActive(true);
        User saved = userRepository.save(user);
        auditService.log("USER_CREATED", "Created account '" + saved.getUsername() + "' (" + role + ")");
        return saved;
    }

    /** Uploads and applies a new profile picture for the currently logged-in user. */
    public void updateAvatar(Path sourceImageFile) throws ValidationException {
        User current = SessionManager.getInstance().getCurrentUser();
        if (current == null) {
            throw new ValidationException("No user is currently signed in.");
        }
        String storedPath = ImageUtil.storeAvatar(sourceImageFile);
        userRepository.updateAvatar(current.getId(), storedPath);
        current.setAvatarPath(storedPath);
        auditService.log("PROFILE_PICTURE_CHANGED", "Updated profile picture");
    }

    /** Changes the current user's password after verifying their current one. */
    public void changePassword(String currentPassword, String newPassword) throws ValidationException {
        User current = SessionManager.getInstance().getCurrentUser();
        if (current == null) {
            throw new ValidationException("No user is currently signed in.");
        }
        if (!PasswordUtil.verify(currentPassword, current.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("New password must be at least 6 characters.");
        }
        String newHash = PasswordUtil.hash(newPassword);
        userRepository.updatePasswordHash(current.getId(), newHash);
        current.setPasswordHash(newHash);
        auditService.log("PASSWORD_CHANGED", "User changed their own password");
    }

    /**
     * Self-service "forgot password" for a desktop app with no email server:
     * generates a random one-time password, applies it immediately, and
     * returns it to the caller to display once so the person can log in and
     * set their own password via My Profile.
     *
     * Deliberately throws a generic ValidationException (not "user not
     * found") for an unknown username, matching the same anti-enumeration
     * posture as login() - this is a demo/internal-tool convenience, not a
     * production password-recovery flow, and is documented as such.
     */
    public String resetPasswordByUsername(String username) throws ValidationException {
        ValidationUtil.requireNonEmpty(username, "Username");
        Optional<User> found = userRepository.findByUsername(username.trim());
        if (found.isEmpty() || !found.get().isActive()) {
            throw new ValidationException("No active account found with that username.");
        }
        User user = found.get();
        String tempPassword = generateTemporaryPassword();
        userRepository.updatePasswordHash(user.getId(), PasswordUtil.hash(tempPassword));
        auditService.log(username, "PASSWORD_RESET", "Temporary password issued via Forgot Password");
        return tempPassword;
    }

    private String generateTemporaryPassword() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
