
package com.hms.repository;

import com.hms.database.DatabaseConnection;
import com.hms.model.User;
import com.hms.model.UserRole;
import com.hms.util.DataAccessException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to look up user by username.", e);
        }
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    public List<User> findAll() {
        return search(null, "All Roles", "All Status", "Full Name (A-Z)");
    }

    public List<User> search(String keyword, String roleFilter, String statusFilter, String sortBy) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // 1. Search text across username, full_name, and email
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR full_name LIKE ? OR email LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        // 2. Role filter
        if (roleFilter != null && !roleFilter.isBlank() && !roleFilter.equalsIgnoreCase("All Roles")) {
            sql.append(" AND role = ?");
            params.add(roleFilter.trim());
        }

        // 3. Status filter
        if ("Active".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND active = 1");
        } else if ("Inactive".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND active = 0");
        }

        // 4. Sorting logic
        if ("Full Name (Z-A)".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY full_name DESC");
        } else if ("Username (A-Z)".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY username ASC");
        } else if ("Role".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY role ASC, full_name ASC");
        } else {
            // Default: Full Name (A-Z)
            sql.append(" ORDER BY full_name ASC");
        }

        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to search users.", e);
        }
        return users;
    }

    public void updateStatus(int userId, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update user status.", e);
        }
    }

    public User save(User user) {
        String sql = "INSERT INTO users (username, password_hash, role, full_name, email, active) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setBoolean(6, user.isActive());
            ps.executeUpdate();
            user.setId(DatabaseConnection.lastInsertRowId(DatabaseConnection.getConnection()));
            return user;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user.", e);
        }
    }

    public void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update last login timestamp.", e);
        }
    }

    public void updateAvatar(int userId, String avatarPath) {
        String sql = "UPDATE users SET avatar_path = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, avatarPath);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update profile picture.", e);
        }
    }

    public void updatePasswordHash(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update password.", e);
        }
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to look up user by id.", e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setActive(rs.getBoolean("active"));
        user.setAvatarPath(rs.getString("avatar_path"));
        return user;
    }
}