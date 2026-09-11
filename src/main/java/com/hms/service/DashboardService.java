package com.hms.service;

import com.hms.database.DatabaseConnection;
import com.hms.model.Room;
import com.hms.util.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates counts for the dashboard summary cards, trend comparisons,
 * and mini-chart data. Kept as a thin facade over the other services
 * rather than querying repositories directly, so the dashboard stays
 * decoupled from persistence details.
 */
public class DashboardService {

    private final PatientService patientService = new PatientService();
    private final DoctorService doctorService = new DoctorService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final RoomService roomService = new RoomService();

    public DashboardStats getStats() {
        double thisMonthRevenue = revenueForMonth(LocalDate.now());
        double lastMonthRevenue = revenueForMonth(LocalDate.now().minusMonths(1));

        List<Room> rooms = roomService.getAllRooms();
        int totalBeds = rooms.stream().mapToInt(Room::getCapacity).sum();
        int occupiedBeds = rooms.stream().mapToInt(Room::getOccupantCount).sum();

        return new DashboardStats(
                patientService.countPatients(),
                doctorService.countDoctors(),
                appointmentService.countTotal(),
                appointmentService.countToday(),
                thisMonthRevenue,
                lastMonthRevenue,
                occupiedBeds,
                totalBeds
        );
    }

    /** Appointment count per day for the last 7 days (oldest first) - feeds the dashboard's mini trend chart. */
    public Map<String, Integer> appointmentsLast7Days() {
        Map<String, Integer> result = new LinkedHashMap<>();
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("EEE");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            int count = appointmentService.getAppointmentsForDate(day).size();
            result.put(day.format(labelFormat), count);
        }
        return result;
    }

    /**
     * The most recently added patients and doctors, interleaved into one
     * feed for the dashboard's Recent Registrations panel (matches the
     * "Shown All / Patients / Doctors" toggle in the UI mockup).
     */
    public List<com.hms.model.RecentRegistration> getRecentRegistrations(int limitPerType) {
        List<com.hms.model.RecentRegistration> combined = new java.util.ArrayList<>();
        for (com.hms.model.Patient p : patientService.getRecentPatients(limitPerType)) {
            combined.add(com.hms.model.RecentRegistration.fromPatient(p));
        }
        for (com.hms.model.Doctor d : doctorService.getRecentDoctors(limitPerType)) {
            combined.add(com.hms.model.RecentRegistration.fromDoctor(d));
        }
        combined.sort((a, b) -> b.addedOn().compareTo(a.addedOn()));
        return combined;
    }

    private double revenueForMonth(LocalDate monthReference) {
        String sql = "SELECT COALESCE(SUM(total), 0) AS revenue FROM invoices WHERE strftime('%Y-%m', issued_on) = ?";
        String yearMonth = monthReference.toString().substring(0, 7);
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, yearMonth);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("revenue") : 0.0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute monthly revenue.", e);
        }
    }

    public record DashboardStats(int totalPatients, int totalDoctors, int totalAppointments,
                                  int todayAppointments, double monthlyRevenue, double previousMonthRevenue,
                                  int occupiedBeds, int totalBeds) {

        /** Percentage change vs last month, for the trend arrow on the revenue card. */
        public double revenueTrendPercent() {
            if (previousMonthRevenue <= 0) {
                return monthlyRevenue > 0 ? 100.0 : 0.0;
            }
            return ((monthlyRevenue - previousMonthRevenue) / previousMonthRevenue) * 100.0;
        }
    }
}
