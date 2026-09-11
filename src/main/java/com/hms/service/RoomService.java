package com.hms.service;

import com.hms.model.Room;
import com.hms.repository.RoomRepository;
import com.hms.util.ValidationException;
import com.hms.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

public class RoomService {

    private final RoomRepository roomRepository = new RoomRepository();

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getAvailableRooms() {
        return roomRepository.findAvailable();
    }

    public Optional<Room> findById(int id) {
        return roomRepository.findById(id);
    }

    public Room addRoom(Room room) throws ValidationException {
        validate(room);
        if (roomRepository.existsByCode(room.getRoomCode())) {
            throw new ValidationException("Room code '" + room.getRoomCode() + "' is already in use.");
        }
        return roomRepository.save(room);
    }

    public void updateRoom(Room room) throws ValidationException {
        validate(room);
        roomRepository.update(room);
    }

    public void deleteRoom(int id) {
        roomRepository.deleteById(id);
    }

    public int countRooms() {
        return roomRepository.countTotal();
    }

    /** Admits a patient into a room, refusing if the room is already at capacity. */
    public void admitPatient(int patientId, int roomId) throws ValidationException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found."));
        if (room.isFull()) {
            throw new ValidationException("Room " + room.getRoomCode() + " is already at full capacity ("
                    + room.getCapacity() + " bed(s)).");
        }
        roomRepository.assignPatient(patientId, roomId);
    }

    /** Discharges a patient, freeing up their bed. */
    public void dischargePatient(int patientId) {
        roomRepository.releasePatient(patientId);
    }

    private void validate(Room room) throws ValidationException {
        ValidationUtil.requireNonEmpty(room.getRoomCode(), "Room code");
        if (room.getRoomType() == null) {
            throw new ValidationException("Please select a room type.");
        }
        if (room.getCapacity() < 1) {
            throw new ValidationException("Capacity must be at least 1.");
        }
    }
}
