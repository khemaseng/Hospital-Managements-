package com.hms.repository;

import com.hms.database.DatabaseConnection;
import com.hms.model.Patient;
import com.hms.util.DataAccessException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientRepository {

    // Aliased join to rooms so callers can see which room (if any) a patient
    // currently occupies without a separate query. "p." prefix on every
    // patients column avoids an id collision with rooms.id.
    private static final String SELECT_WITH_ROOM =
            "SELECT p.*, r.room_code AS assigned_room_code FROM patients p LEFT JOIN rooms r ON r.id = p.room_id ";

    public List<Patient> findAll() {
        String sql = SELECT_WITH_ROOM + "ORDER BY p.id DESC";
        List<Patient> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load patients.", e);
        }
        return result;
    }

    /** Most recently registered patients, newest first - feeds the dashboard's Recent Registrations panel. */
    public List<Patient> findRecent(int limit) {
        String sql = SELECT_WITH_ROOM + "ORDER BY p.id DESC LIMIT ?";
        List<Patient> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load recent patients.", e);
        }
        return result;
    }

    /**
     * Search across name, patient code, and phone; simultaneously filter by
     * gender and blood group when those filters are non-null. Paginated:
     * pageIndex is 0-based, pageSize is rows per page.
     */
    public List<Patient> search(String keyword, String genderFilter, String bloodGroupFilter, int pageIndex, int pageSize) {
        StringBuilder sql = new StringBuilder(SELECT_WITH_ROOM + "WHERE 1=1");
        List<Object> params = buildFilterParams(sql, keyword, genderFilter, bloodGroupFilter);
        sql.append(" ORDER BY p.full_name LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(pageIndex * pageSize);

        List<Patient> result = new ArrayList<>();
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
            throw new DataAccessException("Failed to search patients.", e);
        }
        return result;
    }

    /** Total row count matching the same filters as search(), for computing page count. */
    public int countSearch(String keyword, String genderFilter, String bloodGroupFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS cnt FROM patients p WHERE 1=1");
        List<Object> params = buildFilterParams(sql, keyword, genderFilter, bloodGroupFilter);
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count patients.", e);
        }
    }

    private List<Object> buildFilterParams(StringBuilder sql, String keyword, String genderFilter, String bloodGroupFilter) {
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.full_name LIKE ? OR p.patient_code LIKE ? OR p.phone LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (genderFilter != null && !genderFilter.isBlank() && !genderFilter.equals("All")) {
            sql.append(" AND p.gender = ?");
            params.add(genderFilter);
        }
        if (bloodGroupFilter != null && !bloodGroupFilter.isBlank() && !bloodGroupFilter.equals("All")) {
            sql.append(" AND p.blood_group = ?");
            params.add(bloodGroupFilter);
        }
        return params;
    }

    public Optional<Patient> findById(int id) {
        String sql = SELECT_WITH_ROOM + "WHERE p.id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to look up patient.", e);
        }
    }

    public boolean existsByCode(String patientCode) {
        String sql = "SELECT 1 FROM patients WHERE patient_code = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, patientCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check patient code uniqueness.", e);
        }
    }

    public String nextPatientCode() {
        String sql = "SELECT COUNT(*) AS cnt FROM patients";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int count = rs.next() ? rs.getInt("cnt") : 0;
            return String.format("PAT-%04d", count + 1);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to generate patient code.", e);
        }
    }

    public Patient save(Patient p) {
        String sql = "INSERT INTO patients (patient_code, full_name, gender, age, date_of_birth, phone, " +
                     "email, address, blood_group, emergency_contact) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            bind(ps, p);
            ps.executeUpdate();
            p.setId(DatabaseConnection.lastInsertRowId(DatabaseConnection.getConnection()));
            return p;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save patient.", e);
        }
    }

    public void update(Patient p) {
        String sql = "UPDATE patients SET full_name=?, gender=?, age=?, date_of_birth=?, phone=?, email=?, " +
                     "address=?, blood_group=?, emergency_contact=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getFullName());
            ps.setString(2, p.getGender());
            ps.setInt(3, p.getAge());
            ps.setString(4, p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : null);
            ps.setString(5, p.getPhone());
            ps.setString(6, p.getEmail());
            ps.setString(7, p.getAddress());
            ps.setString(8, p.getBloodGroup());
            ps.setString(9, p.getEmergencyContact());
            ps.setInt(10, p.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Patient with id " + p.getId() + " not found.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update patient.", e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM patients WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete patient. It may have existing appointments.", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) AS cnt FROM patients";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("cnt") : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count patients.", e);
        }
    }

    private void bind(PreparedStatement ps, Patient p) throws SQLException {
        ps.setString(1, p.getPatientCode());
        ps.setString(2, p.getFullName());
        ps.setString(3, p.getGender());
        ps.setInt(4, p.getAge());
        ps.setString(5, p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : null);
        ps.setString(6, p.getPhone());
        ps.setString(7, p.getEmail());
        ps.setString(8, p.getAddress());
        ps.setString(9, p.getBloodGroup());
        ps.setString(10, p.getEmergencyContact());
    }

    private Patient map(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setPatientCode(rs.getString("patient_code"));
        p.setFullName(rs.getString("full_name"));
        p.setGender(rs.getString("gender"));
        p.setAge(rs.getInt("age"));
        String dob = rs.getString("date_of_birth");
        if (dob != null) {
            p.setDateOfBirth(LocalDate.parse(dob));
        }
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setEmergencyContact(rs.getString("emergency_contact"));
        String registered = rs.getString("registered_on");
        if (registered != null) {
            p.setRegisteredOn(LocalDate.parse(registered));
        }
        int roomId = rs.getInt("room_id");
        p.setRoomId(rs.wasNull() ? null : roomId);
        p.setRoomCode(rs.getString("assigned_room_code"));
        return p;
    }
}
