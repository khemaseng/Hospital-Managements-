
package com.hms.repository;

import com.hms.database.DatabaseConnection;
import com.hms.model.MedicalRecord;
import com.hms.util.DataAccessException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicalRecordRepository {

    private static final String SELECT_JOINED =
            "SELECT m.*, p.full_name AS patient_name, d.full_name AS doctor_name " +
                    "FROM medical_records m " +
                    "JOIN patients p ON p.id = m.patient_id " +
                    "JOIN doctors d ON d.id = m.doctor_id ";

    public List<MedicalRecord> findAll() {
        return search(null, "All Doctors", "Date (Newest)");
    }

    public List<MedicalRecord> findByPatient(int patientId) {
        String sql = SELECT_JOINED + "WHERE m.patient_id = ? ORDER BY m.record_date DESC, m.id DESC";
        List<MedicalRecord> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load records for patient.", e);
        }
        return result;
    }

    public List<MedicalRecord> search(String keyword, String doctorFilter, String sortBy) {
        StringBuilder sql = new StringBuilder(SELECT_JOINED + "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // 1. Text Search Across Patient, Doctor, Diagnosis, and Prescription
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (p.full_name LIKE ? OR d.full_name LIKE ? OR m.diagnosis LIKE ? OR m.prescription LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        // 2. Doctor Dropdown Filter
        if (doctorFilter != null && !doctorFilter.isBlank() && !doctorFilter.equalsIgnoreCase("All Doctors")) {
            sql.append(" AND d.full_name = ?");
            params.add(doctorFilter.trim());
        }

        // 3. Sorting Logic
        if ("Date (Oldest)".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY m.record_date ASC, m.id ASC");
        } else if ("Patient (A-Z)".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY p.full_name ASC");
        } else if ("Doctor (A-Z)".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY d.full_name ASC");
        } else {
            // Default: Timeline Newest First
            sql.append(" ORDER BY m.record_date DESC, m.id DESC");
        }

        List<MedicalRecord> result = new ArrayList<>();
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
            throw new DataAccessException("Failed to search medical records.", e);
        }
        return result;
    }

    // Overload for backward compatibility
    public List<MedicalRecord> search(String keyword) {
        return search(keyword, "All Doctors", "Date (Newest)");
    }

    public Optional<MedicalRecord> findById(int id) {
        String sql = SELECT_JOINED + "WHERE m.id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to look up medical record.", e);
        }
    }

    public MedicalRecord save(MedicalRecord r) {
        String sql = "INSERT INTO medical_records (patient_id, doctor_id, appointment_id, diagnosis, " +
                "symptoms, prescription, lab_result, doctor_notes, record_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, r.getPatientId());
            ps.setInt(2, r.getDoctorId());
            if (r.getAppointmentId() > 0) {
                ps.setInt(3, r.getAppointmentId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, r.getDiagnosis());
            ps.setString(5, r.getSymptoms() != null ? r.getSymptoms() : "");
            ps.setString(6, r.getPrescription() != null ? r.getPrescription() : "");
            ps.setString(7, r.getLabResult() != null ? r.getLabResult() : "");
            ps.setString(8, r.getDoctorNotes() != null ? r.getDoctorNotes() : "");
            ps.setString(9, r.getRecordDate() != null ? r.getRecordDate().toString() : LocalDate.now().toString());
            ps.executeUpdate();
            r.setId(DatabaseConnection.lastInsertRowId(DatabaseConnection.getConnection()));
            return r;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save medical record.", e);
        }
    }

    public void update(MedicalRecord r) {
        String sql = "UPDATE medical_records SET patient_id = ?, doctor_id = ?, diagnosis = ?, symptoms = ?, " +
                "prescription = ?, lab_result = ?, doctor_notes = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, r.getPatientId());
            ps.setInt(2, r.getDoctorId());
            ps.setString(3, r.getDiagnosis());
            ps.setString(4, r.getSymptoms());
            ps.setString(5, r.getPrescription());
            ps.setString(6, r.getLabResult());
            ps.setString(7, r.getDoctorNotes());
            ps.setInt(8, r.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update medical record.", e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM medical_records WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete medical record.", e);
        }
    }

    private MedicalRecord map(ResultSet rs) throws SQLException {
        MedicalRecord r = new MedicalRecord();
        r.setId(rs.getInt("id"));
        int apptId = rs.getInt("appointment_id");
        r.setAppointmentId(rs.wasNull() ? 0 : apptId);
        r.setPatientId(rs.getInt("patient_id"));
        r.setPatientName(rs.getString("patient_name"));
        r.setDoctorId(rs.getInt("doctor_id"));
        r.setDoctorName(rs.getString("doctor_name"));
        r.setDiagnosis(rs.getString("diagnosis"));
        r.setSymptoms(rs.getString("symptoms"));
        r.setPrescription(rs.getString("prescription"));
        r.setLabResult(rs.getString("lab_result"));
        r.setDoctorNotes(rs.getString("doctor_notes"));
        String d = rs.getString("record_date");
        if (d != null) {
            r.setRecordDate(LocalDate.parse(d));
        }
        return r;
    }
}