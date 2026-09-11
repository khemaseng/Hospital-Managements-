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

/**
 * Data access layer for the users table. Every query uses PreparedStatement
 * with bound parameters — no string concatenation of SQL, ever — to
 * eliminate SQL-injection risk.
 */
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
        String sql = "SELECT * FROM users ORDER BY full_name";
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load users.", e);
        }
        return users;
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
