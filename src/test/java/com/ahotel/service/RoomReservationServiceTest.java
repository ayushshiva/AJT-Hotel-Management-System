package com.ahotel.service;

import com.ahotel.model.Customer;
import com.ahotel.model.Reservation;
import com.ahotel.model.Room;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RoomReservationServiceTest {
    @Autowired private RoomService rooms;
    @Autowired private ReservationService reservations;
    @Autowired private CustomerService customers;

    @Test
    void createsFiltersAndSafelyDeletesRooms() {
        Room room = room("T" + Long.toString(System.nanoTime()).substring(0, 10));
        long id = rooms.create(room);
        assertEquals(id, rooms.findById(id).getId());
        assertEquals(1, rooms.findAll(room.getRoomNumber(), "Single", "AVAILABLE").size());
        assertThrows(IllegalArgumentException.class, () -> rooms.create(room(room.getRoomNumber())));
        rooms.delete(id);
        assertThrows(java.util.NoSuchElementException.class, () -> rooms.findById(id));
    }

    @Test
    void preventsOverlapsAndSupportsReservationLifecycle() {
        Customer customer = customer();
        long customerId = customers.create(customer);
        Room room = room("R" + Long.toString(System.nanoTime()).substring(0, 10));
        long roomId = rooms.create(room);
        LocalDate start = LocalDate.now().plusDays(10);

        assertTrue(reservations.isAvailable(roomId, start, start.plusDays(2)));
        Reservation first = reservation(customerId, roomId, start, start.plusDays(2));
        long firstId = reservations.create(first);
        Reservation saved = reservations.findById(firstId);
        assertEquals(2, saved.getNights());
        assertEquals(new BigDecimal("200.00"), saved.getTotalRoomCharges());
        assertThrows(IllegalArgumentException.class,
                () -> reservations.create(reservation(customerId, roomId, start.plusDays(1), start.plusDays(3))));
        assertFalse(reservations.isAvailable(roomId, start.plusDays(1), start.plusDays(3)));
        assertTrue(reservations.isAvailable(roomId, start.plusDays(2), start.plusDays(4)));

        assertThrows(IllegalArgumentException.class, () -> reservations.changeStatus(firstId, "check-out"));
        reservations.changeStatus(firstId, "check-in");
        assertEquals("OCCUPIED", rooms.findById(roomId).getStatus());
        reservations.changeStatus(firstId, "check-out");
        assertEquals("CHECKED_OUT", reservations.findById(firstId).getStatus());
        assertEquals("AVAILABLE", rooms.findById(roomId).getStatus());

        Reservation second = reservation(customerId, roomId, start, start.plusDays(2));
        long secondId = reservations.create(second);
        reservations.changeStatus(secondId, "cancel");
        assertEquals("CANCELLED", reservations.findById(secondId).getStatus());
        long replacementId = reservations.create(reservation(customerId, roomId, start, start.plusDays(2)));
        assertEquals("RESERVED", reservations.findById(replacementId).getStatus());
        assertThrows(IllegalArgumentException.class, () -> rooms.delete(roomId));
        assertFalse(customers.deleteOrDeactivate(customerId));
        assertEquals("INACTIVE", customers.findById(customerId).getStatus());
    }

    private Room room(String number) {
        Room room = new Room();
        room.setRoomNumber(number);
        room.setRoomType("Single");
        room.setFloorNumber(1);
        room.setCapacity(1);
        room.setPricePerNight(new BigDecimal("100.00"));
        room.setStatus("AVAILABLE");
        return room;
    }

    private Customer customer() {
        String suffix = Long.toString(System.nanoTime());
        Customer customer = new Customer();
        customer.setFullName("Reservation Test");
        customer.setMobileNumber("9" + suffix.substring(0, 9));
        customer.setEmailAddress("reservation." + suffix + "@example.com");
        customer.setStatus("ACTIVE");
        return customer;
    }

    private Reservation reservation(long customerId, long roomId, LocalDate start, LocalDate end) {
        Reservation reservation = new Reservation();
        reservation.setCustomerId(customerId);
        reservation.setRoomId(roomId);
        reservation.setCheckIn(start);
        reservation.setCheckOut(end);
        return reservation;
    }
}
