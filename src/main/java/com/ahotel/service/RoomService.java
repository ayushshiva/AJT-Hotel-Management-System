package com.ahotel.service;

import com.ahotel.model.Room;
import com.ahotel.repository.RoomRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class RoomService {
    private static final Set<String> TYPES = Set.of("Single", "Double", "Deluxe", "Suite");
    private static final Set<String> STATUSES = Set.of("AVAILABLE", "RESERVED", "OCCUPIED", "MAINTENANCE");
    private final RoomRepository rooms;

    public RoomService(RoomRepository rooms) {
        this.rooms = rooms;
    }

    @Transactional(readOnly = true)
    public List<Room> findAll(String search, String type, String status) {
        if (type != null && !type.isBlank() && !TYPES.contains(type)) {
            throw new IllegalArgumentException("Select a valid room type.");
        }
        String normalizedStatus = status == null || status.isBlank() ? null : status.toUpperCase(Locale.ROOT);
        if (normalizedStatus != null && !STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Select a valid room status.");
        }
        return rooms.findAll(search, type, normalizedStatus);
    }

    @Transactional(readOnly = true)
    public Room findById(long id) {
        return rooms.findById(id).orElseThrow(() -> new NoSuchElementException("Room not found."));
    }

    @Transactional
    public long create(Room room) {
        normalizeAndValidate(room);
        if (rooms.numberExists(room.getRoomNumber(), null)) {
            throw new IllegalArgumentException("Room number already exists.");
        }
        try {
            return rooms.insert(room);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Room number already exists.", e);
        }
    }

    @Transactional
    public void update(long id, Room room) {
        Room existing = findById(id);
        room.setId(id);
        normalizeAndValidate(room);
        if (rooms.countActiveReservations(id) > 0 && !room.getStatus().equals(existing.getStatus())) {
            throw new IllegalArgumentException("Room status is controlled by its active booking; it cannot be changed here.");
        }
        if (rooms.numberExists(room.getRoomNumber(), id)) {
            throw new IllegalArgumentException("Room number already exists.");
        }
        try {
            if (rooms.update(room) != 1) {
                throw new NoSuchElementException("Room not found.");
            }
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Room number already exists.", e);
        }
    }

    @Transactional
    public void delete(long id) {
        findById(id);
        if (rooms.countActiveReservations(id) > 0) {
            throw new IllegalArgumentException("Room has active reservations and cannot be deleted.");
        }
        try {
            if (rooms.delete(id) != 1) {
                throw new NoSuchElementException("Room not found.");
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Room has booking history and cannot be deleted.", e);
        }
    }

    public int count() { return rooms.count(); }
    public int countAvailable() { return rooms.countAvailable(); }

    private void normalizeAndValidate(Room room) {
        room.setRoomNumber(room.getRoomNumber() == null ? null : room.getRoomNumber().trim());
        room.setStatus(room.getStatus() == null || room.getStatus().isBlank()
                ? "AVAILABLE" : room.getStatus().trim().toUpperCase(Locale.ROOT));
        if (room.getRoomNumber() == null || room.getRoomNumber().isBlank() || room.getRoomNumber().length() > 12) {
            throw new IllegalArgumentException("Room number is required and must be 12 characters or fewer.");
        }
        if (room.getRoomType() == null || !TYPES.contains(room.getRoomType())) {
            throw new IllegalArgumentException("Select a valid room type.");
        }
        if (room.getStatus() == null || !STATUSES.contains(room.getStatus())) {
            throw new IllegalArgumentException("Select a valid room status.");
        }
        if (room.getFloorNumber() < 0 || room.getCapacity() < 1) {
            throw new IllegalArgumentException("Floor must be zero or greater and capacity must be positive.");
        }
        BigDecimal price = room.getPricePerNight();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0 || price.scale() > 2) {
            throw new IllegalArgumentException("Price per night must be positive with at most two decimal places.");
        }
    }
}
