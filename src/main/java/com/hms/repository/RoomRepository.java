package com.hms.repository;

import com.hms.database.DatabaseConnection;
import com.hms.model.Room;
import com.hms.model.RoomType;
import com.hms.util.DataAccessException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the rooms table. Occupancy (occupant_count) is computed
 * live via a correlated subquery against patients.room_id rather than
 * stored redundantly, so it can never drift out of sync with reality.
 */
public class RoomRepository {

    private static final String SELECT_WITH_OCCUPANCY =
            "SELECT r.*, (SELECT COUNT(*) FROM patients p WHERE p.room_id = r.id) AS occupant_count FROM rooms r ";

    public List<Room> findAll() {
        String sql = SELECT_WITH_OCCUPANCY + "ORDER BY r.room_code";
        List<Room> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load rooms.", e);
        }
        return result;
    }

    /** Rooms that still have at least one free bed - used to populate the "assign room" dropdown. */
    public List<Room> findAvailable() {
        List<Room> all = findAll();
        all.removeIf(Room::isFull);
        return all;
    }

    public Optional<Room> findById(int id) {
        String sql = SELECT_WITH_OCCUPANCY + "WHERE r.id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to look up room.", e);
        }
    }

    public boolean existsByCode(String roomCode) {
        String sql = "SELECT 1 FROM rooms WHERE room_code = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, roomCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check room code uniqueness.", e);
        }
    }

    public Room save(Room room) {
        String sql = "INSERT INTO rooms (room_code, room_type, capacity, floor, notes) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, room.getRoomCode());
            ps.setString(2, room.getRoomType().name());
            ps.setInt(3, room.getCapacity());
            ps.setString(4, room.getFloor());
            ps.setString(5, room.getNotes());
            ps.executeUpdate();
            room.setId(DatabaseConnection.lastInsertRowId(DatabaseConnection.getConnection()));
            return room;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save room.", e);
        }
    }

    public void update(Room room) {
        String sql = "UPDATE rooms SET room_type=?, capacity=?, floor=?, notes=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, room.getRoomType().name());
            ps.setInt(2, room.getCapacity());
            ps.setString(3, room.getFloor());
            ps.setString(4, room.getNotes());
            ps.setInt(5, room.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update room.", e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete room. It may still have patients assigned.", e);
        }
    }

    /** Assigns a patient to a room (admission). */
    public void assignPatient(int patientId, int roomId) {
        String sql = "UPDATE patients SET room_id = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, patientId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to assign patient to room.", e);
        }
    }

    /** Releases a patient from their current room (discharge). */
    public void releasePatient(int patientId) {
        String sql = "UPDATE patients SET room_id = NULL WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to release patient from room.", e);
        }
    }

    public int countTotal() {
        String sql = "SELECT COUNT(*) AS cnt FROM rooms";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("cnt") : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count rooms.", e);
        }
    }

    private Room map(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getInt("id"));
        room.setRoomCode(rs.getString("room_code"));
        room.setRoomType(RoomType.valueOf(rs.getString("room_type")));
        room.setCapacity(rs.getInt("capacity"));
        room.setFloor(rs.getString("floor"));
        room.setNotes(rs.getString("notes"));
        room.setOccupantCount(rs.getInt("occupant_count"));
        return room;
    }
}
