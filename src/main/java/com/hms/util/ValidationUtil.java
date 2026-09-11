package com.hms.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Central place for all input-validation rules so the same regex/logic
 * isn't duplicated across controllers.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Accepts formats like +855 12 345 678, 012-345-678, (012) 345 678, etc.
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9()\\-\\s]{7,20}$");

    private ValidationUtil() {
    }

    public static void requireNonEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be empty.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Email format is invalid: " + email);
        }
    }

    public static void validatePhone(String phone) throws ValidationException {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Phone number cannot be empty.");
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new ValidationException("Phone number format is invalid: " + phone);
        }
    }

    public static void validateAge(int age) throws ValidationException {
        if (age < 0 || age > 150) {
            throw new ValidationException("Age must be between 0 and 150.");
        }
    }

    public static void validateNotFutureDate(LocalDate date, String fieldName) throws ValidationException {
        if (date == null) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException(fieldName + " cannot be in the future.");
        }
    }

    public static void validateAppointmentDate(LocalDate date) throws ValidationException {
        if (date == null) {
            throw new ValidationException("Appointment date cannot be empty.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new ValidationException("Appointment date cannot be in the past.");
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }
}
