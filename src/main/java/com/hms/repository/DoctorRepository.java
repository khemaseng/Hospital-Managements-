package com.hms.repository;

import com.hms.database.DatabaseConnection;
import com.hms.model.Doctor;
import com.hms.util.DataAccessException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoctorRepository {

    public List<Doctor> findAll() {
        String sql = "SELECT * FROM doctors ORDER BY full_name";
        List<Doctor> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load doctors.", e);
        }
        return result;
    }

    /** Most recently added doctors, newest first - feeds the dashboard's Recent Registrations panel. */
    public List<Doctor> findRecent(int limit) {
        String sql = "SELECT * FROM doctors ORDER BY id DESC LIMIT ?";
        List<Doctor> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load recent doctors.", e);
        }
        return result;
    }

    public List<Doctor> search(String keyword, String departmentFilter, int pageIndex, int pageSize) {
        StringBuilder sql = new StringBuilder("SELECT * FROM doctors WHERE 1=1");
        List<Object> params = buildFilterParams(sql, keyword, departmentFilter);
        sql.append(" ORDER BY full_name LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(pageIndex * pageSize);

        List<Doctor> result = new ArrayList<>();
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
            throw new DataAccessException("Failed to search doctors.", e);
        }
        return result;
    }

    public int countSearch(String keyword, String departmentFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS cnt FROM doctors WHERE 1=1");
        List<Object> params = buildFilterParams(sql, keyword, departmentFilter);
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count doctors.", e);
        }
    }

    private List<Object> buildFilterParams(StringBuilder sql, String keyword, String departmentFilter) {
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (full_name LIKE ? OR doctor_code LIKE ? OR specialization LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (departmentFilter != null && !departmentFilter.isBlank() && !departmentFilter.equals("All")) {
            sql.append(" AND department = ?");
            params.add(departmentFilter);
        }
        return params;
    }

    public Optional<Doctor> findById(int id) {
        String sql = "SELECT * FROM doctors WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to look up doctor.", e);
        }
    }

    public boolean existsByCode(String doctorCode) {
        String sql = "SELECT 1 FROM doctors WHERE doctor_code = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, doctorCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check doctor code uniqueness.", e);
        }
    }

    public String nextDoctorCode() {
        String sql = "SELECT COUNT(*) AS cnt FROM doctors";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int count = rs.next() ? rs.getInt("cnt") : 0;
            return String.format("DOC-%03d", count + 1);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to generate doctor code.", e);
        }
    }

    public Doctor save(Doctor d) {
        String sql = "INSERT INTO doctors (doctor_code, full_name, department, specialization, phone, email, " +
                     "working_schedule, consultation_fee) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            bind(ps, d);
            ps.executeUpdate();
            d.setId(DatabaseConnection.lastInsertRowId(DatabaseConnection.getConnection()));
            return d;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save doctor.", e);
        }
    }

    public void update(Doctor d) {
        String sql = "UPDATE doctors SET full_name=?, department=?, specialization=?, phone=?, email=?, " +
                     "working_schedule=?, consultation_fee=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getFullName());
            ps.setString(2, d.getDepartment());
            ps.setString(3, d.getSpecialization());
            ps.setString(4, d.getPhone());
            ps.setString(5, d.getEmail());
            ps.setString(6, d.getWorkingSchedule());
            ps.setDouble(7, d.getConsultationFee());
            ps.setInt(8, d.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Doctor with id " + d.getId() + " not found.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update doctor.", e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM doctors WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete doctor. They may have existing appointments.", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) AS cnt FROM doctors";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("cnt") : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count doctors.", e);
        }
    }

    private void bind(PreparedStatement ps, Doctor d) throws SQLException {
        ps.setString(1, d.getDoctorCode());
        ps.setString(2, d.getFullName());
        ps.setString(3, d.getDepartment());
        ps.setString(4, d.getSpecialization());
        ps.setString(5, d.getPhone());
        ps.setString(6, d.getEmail());
        ps.setString(7, d.getWorkingSchedule());
        ps.setDouble(8, d.getConsultationFee());
    }

    private Doctor map(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setId(rs.getInt("id"));
        d.setDoctorCode(rs.getString("doctor_code"));
        d.setFullName(rs.getString("full_name"));
        d.setDepartment(rs.getString("department"));
        d.setSpecialization(rs.getString("specialization"));
        d.setPhone(rs.getString("phone"));
        d.setEmail(rs.getString("email"));
        d.setWorkingSchedule(rs.getString("working_schedule"));
        d.setConsultationFee(rs.getDouble("consultation_fee"));
        return d;
    }
}
