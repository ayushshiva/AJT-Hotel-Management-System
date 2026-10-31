package com.ahotel.service;

import com.ahotel.model.Booking;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final JdbcTemplate db;

    public BookingService(JdbcTemplate db) {
        this.db = db;
    }

    public List<Booking> findAll() {
        return db.query(
                "SELECT id, customer_name, phone, room_number, nights FROM bookings ORDER BY id DESC",
                (rs, rowNum) -> new Booking(
                        rs.getLong("id"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        rs.getString("room_number"),
                        rs.getInt("nights")
                )
        );
    }

    public void create(Booking booking) {
        if (booking.getCustomerName() == null || booking.getCustomerName().isBlank()
                || booking.getPhone() == null || booking.getPhone().isBlank()
                || booking.getRoomNumber() == null || booking.getRoomNumber().isBlank()) {
            throw new IllegalArgumentException("Customer, phone and room are required.");
        }

        if (booking.getNights() < 1 || booking.getNights() > 365) {
            throw new IllegalArgumentException("Nights must be between 1 and 365.");
        }

        Integer existingBookings = db.queryForObject(
                "SELECT COUNT(*) FROM bookings WHERE room_number = ?",
                Integer.class,
                booking.getRoomNumber()
        );

        if (existingBookings != null && existingBookings > 0) {
            throw new IllegalArgumentException("Room already booked in this demo.");
        }

        db.update(
                "INSERT INTO bookings(customer_name, phone, room_number, nights) VALUES(?, ?, ?, ?)",
                booking.getCustomerName().trim(),
                booking.getPhone().trim(),
                booking.getRoomNumber().trim(),
                booking.getNights()
        );
    }

    public int count() {
        Integer total = db.queryForObject("SELECT COUNT(*) FROM bookings", Integer.class);
        return total != null ? total : 0;
    }
}
