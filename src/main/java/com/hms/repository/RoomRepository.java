
package com.hms.repository;

import com.hms.database.DatabaseConnection;
import com.hms.model.Room;
import com.hms.model.RoomType;
import com.hms.util.DataAccessException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomRepository {

    private static final String SELECT_WITH_OCCUPANCY =
            "SELECT r.*, (SELECT COUNT(*) FROM patients p WHERE p.room_id = r.id) AS occupant_count FROM rooms r ";

    public List<Room> findAll() {
        return search(null, "All Types", "All Status", "Room Code (A-Z)");
    }

    public List<Room> findAvailable() {
        return search(null, "All Types", "Available (Has Space)", "Room Code (A-Z)");
    }

    public List<Room> search(String keyword, String typeFilter, String availabilityFilter, String sortBy) {
        StringBuilder sql = new StringBuilder(SELECT_WITH_OCCUPANCY + "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // 1. Keyword search (Room Code, Floor, Notes)
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (r.room_code LIKE ? OR r.floor LIKE ? OR r.notes LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        // 2. Room Type Filter (Resolves by Display Name or Enum Name)
        if (typeFilter != null && !typeFilter.isBlank() && !typeFilter.equalsIgnoreCase("All Types")) {
            String enumName = null;
            for (RoomType rt : RoomType.values()) {
                if (rt.getDisplayName().equalsIgnoreCase(typeFilter.trim()) || rt.name().equalsIgnoreCase(typeFilter.trim())) {
                    enumName = rt.name();
                    break;
                }
            }
            if (enumName != null) {
                sql.append(" AND r.room_type = ?");
                params.add(enumName);
            }
        }

        // 3. Availability Filter
        if ("Available (Has Space)".equalsIgnoreCase(availabilityFilter)) {
            sql.append(" AND (SELECT COUNT(*) FROM patients p WHERE p.room_id = r.id) < r.capacity");
        } else if ("Full (100% Occupied)".equalsIgnoreCase(availabilityFilter)) {
            sql.append(" AND (SELECT COUNT(*) FROM patients p WHERE p.room_id = r.id) >= r.capacity");
        }

        // 4. Sorting logic
        if ("Room Code (Z-A)".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY r.room_code DESC");
        } else if ("Most Available".equalsIgnoreCase(sortBy)) {
            // (capacity - occupant_count) descending
            sql.append(" ORDER BY (r.capacity - (SELECT COUNT(*) FROM patients p WHERE p.room_id = r.id)) DESC");
        } else if ("Highest Occupancy".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY (SELECT COUNT(*) FROM patients p WHERE p.room_id = r.id) DESC");
        } else if ("Floor".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY r.floor ASC, r.room_code ASC");
        } else {
            // Default: Room Code (A-Z)
            sql.append(" ORDER BY r.room_code ASC");
        }

        List<Room> result = new ArrayList<>();
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
            throw new DataAccessException("Failed to search rooms.", e);
        }
        return result;
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