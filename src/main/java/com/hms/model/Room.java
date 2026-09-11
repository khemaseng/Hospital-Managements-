package com.hms.model;

import javafx.beans.property.*;

/**
 * A physical room/bed the hospital can admit a patient into.
 * occupantCount/available are populated by RoomRepository from a live
 * COUNT of patients currently assigned (patients.room_id = this room),
 * not stored directly, so occupancy is always accurate.
 */
public class Room {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty roomCode = new SimpleStringProperty(this, "roomCode");
    private final ObjectProperty<RoomType> roomType = new SimpleObjectProperty<>(this, "roomType");
    private final IntegerProperty capacity = new SimpleIntegerProperty(this, "capacity");
    private final StringProperty floor = new SimpleStringProperty(this, "floor");
    private final StringProperty notes = new SimpleStringProperty(this, "notes");
    private final IntegerProperty occupantCount = new SimpleIntegerProperty(this, "occupantCount");

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getRoomCode() { return roomCode.get(); }
    public void setRoomCode(String value) { roomCode.set(value); }
    public StringProperty roomCodeProperty() { return roomCode; }

    public RoomType getRoomType() { return roomType.get(); }
    public void setRoomType(RoomType value) { roomType.set(value); }
    public ObjectProperty<RoomType> roomTypeProperty() { return roomType; }

    public int getCapacity() { return capacity.get(); }
    public void setCapacity(int value) { capacity.set(value); }
    public IntegerProperty capacityProperty() { return capacity; }

    public String getFloor() { return floor.get(); }
    public void setFloor(String value) { floor.set(value); }
    public StringProperty floorProperty() { return floor; }

    public String getNotes() { return notes.get(); }
    public void setNotes(String value) { notes.set(value); }
    public StringProperty notesProperty() { return notes; }

    public int getOccupantCount() { return occupantCount.get(); }
    public void setOccupantCount(int value) { occupantCount.set(value); }
    public IntegerProperty occupantCountProperty() { return occupantCount; }

    public int getAvailableBeds() {
        return Math.max(0, getCapacity() - getOccupantCount());
    }

    public boolean isFull() {
        return getOccupantCount() >= getCapacity();
    }

    @Override
    public String toString() {
        return getRoomCode() + " (" + (getRoomType() != null ? getRoomType().getDisplayName() : "") + ")";
    }
}
