package com.ahotel.repository;

import com.ahotel.model.Reservation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationRepository {
    private static final RowMapper<Reservation> MAPPER = (rs, row) -> {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getLong("id"));
        reservation.setCustomerId(rs.getLong("customer_id"));
        reservation.setRoomId(rs.getLong("room_id"));
        reservation.setCustomerName(rs.getString("customer_name"));
        reservation.setPhone(rs.getString("phone"));
        reservation.setRoomNumber(rs.getString("room_number"));
        reservation.setCheckIn(rs.getDate("check_in").toLocalDate());
        reservation.setCheckOut(rs.getDate("check_out").toLocalDate());
        reservation.setNights(rs.getInt("nights"));
        reservation.setRoomRate(rs.getBigDecimal("room_rate"));
        reservation.setTotalRoomCharges(rs.getBigDecimal("total_room_charges"));
        reservation.setStatus(rs.getString("status"));
        reservation.setBookingDate(rs.getTimestamp("booking_date").toLocalDateTime());
        return reservation;
    };

    private final JdbcTemplate db;

    public ReservationRepository(JdbcTemplate db) {
        this.db = db;
    }

    public List<Reservation> findAll(String search, String status) {
        StringBuilder sql = new StringBuilder("SELECT r.*, rm.room_number FROM reservations r "
                + "JOIN rooms rm ON rm.id=r.room_id WHERE 1=1");
        java.util.ArrayList<Object> args = new java.util.ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(r.customer_name) LIKE ? OR LOWER(r.phone) LIKE ? OR LOWER(rm.room_number) LIKE ?)");
            String pattern = "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%";
            args.add(pattern); args.add(pattern); args.add(pattern);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND r.status = ?");
            args.add(status);
        }
        sql.append(" ORDER BY r.booking_date DESC, r.id DESC");
        return db.query(sql.toString(), MAPPER, args.toArray());
    }

    public Optional<Reservation> findById(long id) {
        return db.query("SELECT r.*, rm.room_number FROM reservations r JOIN rooms rm ON rm.id=r.room_id WHERE r.id=?",
                MAPPER, id).stream().findFirst();
    }

    public boolean hasOverlap(long roomId, LocalDate start, LocalDate end, Long exceptId) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE room_id=? AND status IN ('RESERVED','CHECKED_IN') "
                + "AND check_in < ? AND check_out > ?" + (exceptId == null ? "" : " AND id <> ?");
        Integer count = exceptId == null
                ? db.queryForObject(sql, Integer.class, roomId, end, start)
                : db.queryForObject(sql, Integer.class, roomId, end, start, exceptId);
        return count != null && count > 0;
    }

    public void lockRoom(long roomId) {
        db.queryForObject("SELECT id FROM rooms WHERE id=? FOR UPDATE", Long.class, roomId);
    }

    public void lockReservation(long reservationId) {
        db.queryForObject("SELECT id FROM reservations WHERE id=? FOR UPDATE", Long.class, reservationId);
    }

    public long insert(Reservation r) {
        KeyHolder key = new GeneratedKeyHolder();
        db.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO reservations(customer_id,room_id,customer_name,phone,check_in,check_out,nights,"
                            + "room_rate,total_room_charges,status) VALUES(?,?,?,?,?,?,?,?,?,'RESERVED')",
                    new String[]{"id"});
            statement.setLong(1, r.getCustomerId());
            statement.setLong(2, r.getRoomId());
            statement.setString(3, r.getCustomerName());
            statement.setString(4, r.getPhone());
            statement.setObject(5, r.getCheckIn());
            statement.setObject(6, r.getCheckOut());
            statement.setInt(7, r.getNights());
            statement.setBigDecimal(8, r.getRoomRate());
            statement.setBigDecimal(9, r.getTotalRoomCharges());
            return statement;
        }, key);
        Number id = key.getKey();
        if (id == null) {
            throw new IllegalStateException("The database did not return a booking ID.");
        }
        return id.longValue();
    }

    public int update(Reservation r) {
        return db.update("UPDATE reservations SET customer_id=?,room_id=?,customer_name=?,phone=?,check_in=?,"
                        + "check_out=?,nights=?,room_rate=?,total_room_charges=? WHERE id=? AND status='RESERVED'",
                r.getCustomerId(), r.getRoomId(), r.getCustomerName(), r.getPhone(), r.getCheckIn(), r.getCheckOut(),
                r.getNights(), r.getRoomRate(), r.getTotalRoomCharges(), r.getId());
    }

    public int setStatus(long id, String status) {
        return db.update("UPDATE reservations SET status=? WHERE id=?", status, id);
    }

    public String getRoomStatus(long roomId) {
        return db.queryForObject("SELECT status FROM rooms WHERE id=?", String.class, roomId);
    }

    public int setRoomStatus(long roomId, String status) {
        return db.update("UPDATE rooms SET status=? WHERE id=?", status, roomId);
    }

    public boolean hasOtherActiveReservation(long roomId, long exceptId) {
        Integer count = db.queryForObject("SELECT COUNT(*) FROM reservations WHERE room_id=? AND id<>? "
                + "AND status='RESERVED'", Integer.class, roomId, exceptId);
        return count != null && count > 0;
    }

    public int countActive() {
        Integer count = db.queryForObject(
                "SELECT COUNT(*) FROM reservations WHERE status IN ('RESERVED','CHECKED_IN')", Integer.class);
        return count == null ? 0 : count;
    }
}
