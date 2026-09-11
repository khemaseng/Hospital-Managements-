package com.hms.service;

import com.hms.model.Appointment;
import com.hms.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Computes in-app notifications on demand from live data rather than
 * persisting a separate notifications table - for a single-user desktop
 * app, "appointments scheduled in the next 24 hours" IS the notification
 * feed, and it can never go stale or need its own read/unread bookkeeping
 * beyond what's already tracked elsewhere.
 */
public class NotificationService {

    private final AppointmentService appointmentService = new AppointmentService();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM");

    public record Notification(String title, String message, LocalDateTime when) {
    }

    /** Scheduled appointments happening today or tomorrow, soonest first. */
    public List<Notification> getUpcomingNotifications() {
        List<Notification> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<Appointment> upcoming = new ArrayList<>();
        upcoming.addAll(appointmentService.getAppointmentsForDate(today));
        upcoming.addAll(appointmentService.getAppointmentsForDate(tomorrow));

        upcoming.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .sorted(Comparator.comparing(Appointment::getAppointmentDate)
                        .thenComparing(Appointment::getAppointmentTime))
                .forEach(a -> {
                    boolean isToday = a.getAppointmentDate().equals(today);
                    String when = (isToday ? "Today" : "Tomorrow") + " at " + a.getAppointmentTime().format(TIME_FORMAT);
                    result.add(new Notification(
                            a.getPatientName() + " with Dr. " + a.getDoctorName(),
                            when + " - " + a.getDepartment(),
                            LocalDateTime.of(a.getAppointmentDate(), a.getAppointmentTime())
                    ));
                });
        return result;
    }

    public int getUpcomingCount() {
        return getUpcomingNotifications().size();
    }
}
