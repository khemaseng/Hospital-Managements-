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
            // Audit logging failure should never break the actual operation it's
            // recording - log-and-continue rather than propagating.
            System.err.println("Failed to write audit log entry: " + e.getMessage());
        }
    }

    public List<AuditLog> findRecent(int limit) {
        String sql = "SELECT * FROM audit_log ORDER BY occurred_at DESC LIMIT ?";
        List<AuditLog> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load audit log.", e);
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
