package com.hms.repository;

import com.hms.database.DatabaseConnection;
import com.hms.model.Appointment;
import com.hms.model.AppointmentStatus;
import com.hms.util.DataAccessException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentRepository {

    private static final String SELECT_JOINED =
            "SELECT a.*, p.full_name AS patient_name, d.full_name AS doctor_name " +
            "FROM appointments a " +
            "JOIN patients p ON p.id = a.patient_id " +
            "JOIN doctors d ON d.id = a.doctor_id ";

    public List<Appointment> findAll() {
        String sql = SELECT_JOINED + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        List<Appointment> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load appointments.", e);
        }
        return result;
    }

    public List<Appointment> findByDate(LocalDate date) {
        String sql = SELECT_JOINED + "WHERE a.appointment_date = ? ORDER BY a.appointment_time";
        List<Appointment> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load appointments for date.", e);
        }
        return result;
    }

    public List<Appointment> search(String keyword, String statusFilter) {
        StringBuilder sql = new StringBuilder(SELECT_JOINED + "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.full_name LIKE ? OR d.full_name LIKE ? OR a.appointment_code LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equals("All")) {
            sql.append(" AND a.status = ?");
            params.add(statusFilter);
        }
        sql.append(" ORDER BY a.appointment_date DESC, a.appointment_time DESC");

        List<Appointment> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to search appointments.", e);
        }
        return result;
    }

    /**
     * True if the given doctor already has a SCHEDULED appointment at the
     * exact date/time requested. Used by AppointmentService to reject
     * double-bookings before an INSERT is attempted (the UNIQUE constraint
     * in the schema is the last line of defense).
     */
    public boolean hasConflict(int doctorId, LocalDate date, LocalTime time, Integer excludeAppointmentId) {
        StringBuilder sql = new StringBuilder(
                "SELECT 1 FROM appointments WHERE doctor_id = ? AND appointment_date = ? " +
                "AND appointment_time = ? AND status = 'SCHEDULED'");
        if (excludeAppointmentId != null) {
            sql.append(" AND id != ?");
        }
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql.toString())) {
            ps.setInt(1, doctorId);
            ps.setString(2, date.toString());
            ps.setString(3, time.toString());
            if (excludeAppointmentId != null) {
                ps.setInt(4, excludeAppointmentId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check appointment conflicts.", e);
        }
    }

    public Optional<Appointment> findById(int id) {
        String sql = SELECT_JOINED + "WHERE a.id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to look up appointment.", e);
        }
    }

    public String nextAppointmentCode() {
        String sql = "SELECT COUNT(*) AS cnt FROM appointments";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int count = rs.next() ? rs.getInt("cnt") : 0;
            return String.format("APT-%05d", count + 1);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to generate appointment code.", e);
        }
    }

    public Appointment save(Appointment a) {
        String sql = "INSERT INTO appointments (appointment_code, patient_id, doctor_id, department, " +
                     "appointment_date, appointment_time, status, reason) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, a.getAppointmentCode());
            ps.setInt(2, a.getPatientId());
            ps.setInt(3, a.getDoctorId());
            ps.setString(4, a.getDepartment());
            ps.setString(5, a.getAppointmentDate().toString());
            ps.setString(6, a.getAppointmentTime().toString());
            ps.setString(7, a.getStatus().name());
            ps.setString(8, a.getReason());
            ps.executeUpdate();
            a.setId(DatabaseConnection.lastInsertRowId(DatabaseConnection.getConnection()));
            return a;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save appointment.", e);
        }
    }

    public void updateStatus(int appointmentId, AppointmentStatus status) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, appointmentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update appointment status.", e);
        }
    }

    public void reschedule(int appointmentId, LocalDate newDate, LocalTime newTime) {
        String sql = "UPDATE appointments SET appointment_date = ?, appointment_time = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, newDate.toString());
            ps.setString(2, newTime.toString());
            ps.setInt(3, appointmentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to reschedule appointment.", e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM appointments WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete appointment.", e);
        }
    }

    public int countTotal() {
        return countWhere("SELECT COUNT(*) AS cnt FROM appointments");
    }

    public int countToday() {
        String sql = "SELECT COUNT(*) AS cnt FROM appointments WHERE appointment_date = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count today's appointments.", e);
        }
    }

    private int countWhere(String sql) {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("cnt") : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count appointments.", e);
        }
    }

    private Appointment map(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setAppointmentCode(rs.getString("appointment_code"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setPatientName(rs.getString("patient_name"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setDoctorName(rs.getString("doctor_name"));
        a.setDepartment(rs.getString("department"));
        a.setAppointmentDate(LocalDate.parse(rs.getString("appointment_date")));
        a.setAppointmentTime(LocalTime.parse(rs.getString("appointment_time")));
        a.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
        a.setReason(rs.getString("reason"));
        return a;
    }
}
