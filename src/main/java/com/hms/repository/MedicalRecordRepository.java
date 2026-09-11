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
            "SELECT r.*, p.full_name AS patient_name, d.full_name AS doctor_name " +
            "FROM medical_records r " +
            "JOIN patients p ON p.id = r.patient_id " +
            "JOIN doctors d ON d.id = r.doctor_id ";

    public List<MedicalRecord> findAll() {
        String sql = SELECT_JOINED + "ORDER BY r.record_date DESC, r.id DESC";
        List<MedicalRecord> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load medical records.", e);
        }
        return result;
    }

    public List<MedicalRecord> findByPatient(int patientId) {
        String sql = SELECT_JOINED + "WHERE r.patient_id = ? ORDER BY r.record_date DESC";
        List<MedicalRecord> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load medical records for patient.", e);
        }
        return result;
    }

    public List<MedicalRecord> search(String keyword) {
        StringBuilder sql = new StringBuilder(SELECT_JOINED + "WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.full_name LIKE ? OR d.full_name LIKE ? OR r.diagnosis LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY r.record_date DESC, r.id DESC");

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

    public Optional<MedicalRecord> findById(int id) {
        String sql = SELECT_JOINED + "WHERE r.id = ?";
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
        String sql = "INSERT INTO medical_records (appointment_id, patient_id, doctor_id, diagnosis, symptoms, " +
                     "prescription, lab_result, doctor_notes, record_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            if (r.getAppointmentId() > 0) {
                ps.setInt(1, r.getAppointmentId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, r.getPatientId());
            ps.setInt(3, r.getDoctorId());
            ps.setString(4, r.getDiagnosis());
            ps.setString(5, r.getSymptoms());
            ps.setString(6, r.getPrescription());
            ps.setString(7, r.getLabResult());
            ps.setString(8, r.getDoctorNotes());
            ps.setString(9, (r.getRecordDate() != null ? r.getRecordDate() : LocalDate.now()).toString());
            ps.executeUpdate();
            r.setId(DatabaseConnection.lastInsertRowId(DatabaseConnection.getConnection()));
            return r;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save medical record.", e);
        }
    }

    public void update(MedicalRecord r) {
        String sql = "UPDATE medical_records SET diagnosis=?, symptoms=?, prescription=?, lab_result=?, " +
                     "doctor_notes=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, r.getDiagnosis());
            ps.setString(2, r.getSymptoms());
            ps.setString(3, r.getPrescription());
            ps.setString(4, r.getLabResult());
            ps.setString(5, r.getDoctorNotes());
            ps.setInt(6, r.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Medical record with id " + r.getId() + " not found.");
            }
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
        r.setAppointmentId(rs.getInt("appointment_id"));
        r.setPatientId(rs.getInt("patient_id"));
        r.setPatientName(rs.getString("patient_name"));
        r.setDoctorId(rs.getInt("doctor_id"));
        r.setDoctorName(rs.getString("doctor_name"));
        r.setDiagnosis(rs.getString("diagnosis"));
        r.setSymptoms(rs.getString("symptoms"));
        r.setPrescription(rs.getString("prescription"));
        r.setLabResult(rs.getString("lab_result"));
        r.setDoctorNotes(rs.getString("doctor_notes"));
        String date = rs.getString("record_date");
        if (date != null) {
            r.setRecordDate(LocalDate.parse(date));
        }
        return r;
    }
}
