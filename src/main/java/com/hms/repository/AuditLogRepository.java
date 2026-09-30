
package com.hms.repository;

import com.hms.database.DatabaseConnection;
import com.hms.model.AuditLog;
import com.hms.util.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogRepository {

    public void record(Integer userId, String username, String action, String details) {
        String sql = "INSERT INTO audit_log (user_id, username, action, details) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, username);
            ps.setString(3, action);
            ps.setString(4, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to write audit log entry: " + e.getMessage());
        }
    }

    public List<AuditLog> findRecent(int limit) {
        return search(null, "All Actions", "Time (Newest)", limit);
    }

    public List<AuditLog> search(String keyword, String actionFilter, String sortBy, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_log WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // 1. Text search across username, action, and details
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR action LIKE ? OR details LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        // 2. Action filter matching
        if (actionFilter != null && !actionFilter.isBlank() && !actionFilter.equalsIgnoreCase("All Actions")) {
            sql.append(" AND action LIKE ?");
            params.add("%" + actionFilter.trim() + "%");
        }

        // 3. Sorting logic
        if ("Time (Oldest)".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY occurred_at ASC");
        } else if ("User (A-Z)".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY username ASC, occurred_at DESC");
        } else if ("Action (A-Z)".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY action ASC, occurred_at DESC");
        } else {
            // Default: Time (Newest)
            sql.append(" ORDER BY occurred_at DESC");
        }

        sql.append(" LIMIT ?");
        params.add(limit > 0 ? limit : 200);

        List<AuditLog> result = new ArrayList<>();
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
            throw new DataAccessException("Failed to search audit log.", e);
        }
        return result;
    }

    private AuditLog map(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getInt("id"));
        int userId = rs.getInt("user_id");
        log.setUserId(rs.wasNull() ? null : userId);
        log.setUsername(rs.getString("username"));
        log.setAction(rs.getString("action"));
        log.setDetails(rs.getString("details"));
        String occurred = rs.getString("occurred_at");
        if (occurred != null) {
            log.setOccurredAt(LocalDateTime.parse(occurred.replace(' ', 'T')));
        }
        return log;
    }
}