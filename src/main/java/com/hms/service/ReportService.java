package com.hms.service;

import com.hms.database.DatabaseConnection;
import com.hms.util.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Read-only aggregation queries backing the Reports screen's charts.
 * Deliberately kept separate from AppointmentService/DoctorService since
 * these are reporting-shaped GROUP BY queries, not per-entity CRUD.
 */
public class ReportService {

    /** Appointment count per status (Scheduled/Completed/Cancelled/No-show) - feeds the pie chart. */
    public Map<String, Integer> appointmentsByStatus() {
        String sql = "SELECT status, COUNT(*) AS cnt FROM appointments GROUP BY status";
        return groupByCount(sql);
    }

    /** Appointment count per department - feeds the bar chart. */
    public Map<String, Integer> appointmentsByDepartment() {
        String sql = "SELECT department, COUNT(*) AS cnt FROM appointments GROUP BY department ORDER BY cnt DESC";
        return groupByCount(sql, "department");
    }

    /** Patient count per gender - feeds a small pie chart. */
    public Map<String, Integer> patientsByGender() {
        String sql = "SELECT gender, COUNT(*) AS cnt FROM patients GROUP BY gender";
        return groupByCount(sql, "gender");
    }

    public int appointmentsInLastNDays(int days) {
        String sql = "SELECT COUNT(*) AS cnt FROM appointments WHERE appointment_date >= ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().minusDays(days).toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute report range.", e);
        }
    }

    public double revenueInLastNDays(int days) {
        String sql = "SELECT COALESCE(SUM(total), 0) AS revenue FROM invoices WHERE issued_on >= ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().minusDays(days).toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("revenue") : 0.0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute revenue report.", e);
        }
    }

    private Map<String, Integer> groupByCount(String sql) {
        return groupByCount(sql, "status");
    }

    private Map<String, Integer> groupByCount(String sql, String labelColumn) {
        Map<String, Integer> result = new LinkedHashMap<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString(labelColumn), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load report data.", e);
        }
        return result;
    }
}
