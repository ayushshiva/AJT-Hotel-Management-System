package com.ahotel.repository;

import com.ahotel.model.Room;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RoomRepository {
    private static final RowMapper<Room> MAPPER = (rs, row) -> {
        Room room = new Room();
        room.setId(rs.getLong("id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomType(rs.getString("room_type"));
        room.setFloorNumber(rs.getInt("floor_number"));
        room.setCapacity(rs.getInt("capacity"));
        room.setPricePerNight(rs.getBigDecimal("price_per_night"));
        room.setDescription(rs.getString("description"));
        room.setAmenities(rs.getString("amenities"));
        room.setStatus(rs.getString("status"));
        room.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return room;
    };

    private final JdbcTemplate db;

    public RoomRepository(JdbcTemplate db) {
        this.db = db;
    }

    public List<Room> findAll(String search, String type, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM rooms WHERE 1=1");
        java.util.ArrayList<Object> args = new java.util.ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(room_number) LIKE ? OR LOWER(description) LIKE ? OR LOWER(amenities) LIKE ?)");
            String pattern = "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%";
            args.add(pattern);
            args.add(pattern);
            args.add(pattern);
        }
        if (type != null && !type.isBlank()) {
            sql.append(" AND room_type = ?");
            args.add(type);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            args.add(status);
        }
        sql.append(" ORDER BY room_number");
        return db.query(sql.toString(), MAPPER, args.toArray());
    }

    public Optional<Room> findById(long id) {
        return db.query("SELECT * FROM rooms WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    public boolean numberExists(String number, Long exceptId) {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_number = ?" + (exceptId == null ? "" : " AND id <> ?");
        Integer count = exceptId == null
                ? db.queryForObject(sql, Integer.class, number)
                : db.queryForObject(sql, Integer.class, number, exceptId);
        return count != null && count > 0;
    }

    public long insert(Room room) {
        KeyHolder key = new GeneratedKeyHolder();
        db.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO rooms(room_number,room_type,floor_number,capacity,price_per_night,description,amenities,status) "
                            + "VALUES(?,?,?,?,?,?,?,?)", new String[]{"id"});
            statement.setString(1, room.getRoomNumber());
            statement.setString(2, room.getRoomType());
            statement.setInt(3, room.getFloorNumber());
            statement.setInt(4, room.getCapacity());
            statement.setBigDecimal(5, room.getPricePerNight());
            statement.setString(6, room.getDescription());
            statement.setString(7, room.getAmenities());
            statement.setString(8, room.getStatus());
            return statement;
        }, key);
        Number id = key.getKey();
        if (id == null) {
            throw new IllegalStateException("The database did not return a room ID.");
        }
        return id.longValue();
    }

    public int update(Room room) {
        return db.update("UPDATE rooms SET room_number=?,room_type=?,floor_number=?,capacity=?,price_per_night=?,"
                        + "description=?,amenities=?,status=? WHERE id=?",
                room.getRoomNumber(), room.getRoomType(), room.getFloorNumber(), room.getCapacity(),
                room.getPricePerNight(), room.getDescription(), room.getAmenities(), room.getStatus(), room.getId());
    }

    public int delete(long id) {
        return db.update("DELETE FROM rooms WHERE id = ?", id);
    }

    public int countActiveReservations(long roomId) {
        Integer count = db.queryForObject(
                "SELECT COUNT(*) FROM reservations WHERE room_id=? AND status IN ('RESERVED','CHECKED_IN')",
                Integer.class, roomId);
        return count == null ? 0 : count;
    }

    public int count() {
        Integer count = db.queryForObject("SELECT COUNT(*) FROM rooms", Integer.class);
        return count == null ? 0 : count;
    }

    public int countAvailable() {
        Integer count = db.queryForObject("SELECT COUNT(*) FROM rooms WHERE status='AVAILABLE'", Integer.class);
        return count == null ? 0 : count;
    }
}
