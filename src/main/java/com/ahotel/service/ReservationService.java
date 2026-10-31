package com.ahotel.service;

import com.ahotel.model.Customer;
import com.ahotel.model.Reservation;
import com.ahotel.model.Room;
import com.ahotel.repository.CustomerRepository;
import com.ahotel.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class ReservationService {
    private static final Set<String> STATUSES = Set.of("RESERVED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED");
    private final ReservationRepository reservations;
    private final CustomerService customers;
    private final CustomerRepository customerRepository;
    private final RoomService rooms;

    public ReservationService(ReservationRepository reservations, CustomerService customers,
                              CustomerRepository customerRepository, RoomService rooms) {
        this.reservations = reservations;
        this.customers = customers;
        this.customerRepository = customerRepository;
        this.rooms = rooms;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAll(String search, String status) {
        String normalized = status == null || status.isBlank() ? null : status.trim().toUpperCase(java.util.Locale.ROOT);
        if (normalized != null && !STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Select a valid booking status.");
        }
        return reservations.findAll(search, normalized);
    }

    @Transactional(readOnly = true)
    public Reservation findById(long id) {
        return reservations.findById(id).orElseThrow(() -> new NoSuchElementException("Booking not found."));
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(long roomId, java.time.LocalDate checkIn, java.time.LocalDate checkOut) {
        Room room = rooms.findById(roomId);
        Reservation probe = new Reservation();
        probe.setCheckIn(checkIn);
        probe.setCheckOut(checkOut);
        validateDates(probe);
        return !"MAINTENANCE".equals(room.getStatus())
                && !"OCCUPIED".equals(room.getStatus())
                && !reservations.hasOverlap(roomId, checkIn, checkOut, null);
    }

    public List<Customer> findActiveCustomers() {
        return customerRepository.findActive();
    }

    @Transactional
    public long create(Reservation reservation) {
        Room room = rooms.findById(requiredId(reservation.getRoomId(), "Select a room."));
        Customer customer = customers.findById(requiredId(reservation.getCustomerId(), "Select a customer."));
        requireActive(customer);
        reservations.lockRoom(room.getId());
        validateDates(reservation);
        validateRoomCanBeReserved(room);
        reservation.setCustomerName(customer.getFullName());
        reservation.setPhone(customer.getMobileNumber());
        reservation.setRoomRate(room.getPricePerNight());
        calculateCharges(reservation);
        if (reservations.hasOverlap(room.getId(), reservation.getCheckIn(), reservation.getCheckOut(), null)) {
            throw new IllegalArgumentException("The selected room already has an overlapping active booking.");
        }
        long id = reservations.insert(reservation);
        if (!"OCCUPIED".equals(room.getStatus())) {
            reservations.setRoomStatus(room.getId(), "RESERVED");
        }
        return id;
    }

    @Transactional
    public void update(long id, Reservation reservation) {
        Reservation existing = findById(id);
        if (!"RESERVED".equals(existing.getStatus())) {
            throw new IllegalArgumentException("Only reserved bookings can be edited.");
        }
        Room room = rooms.findById(requiredId(reservation.getRoomId(), "Select a room."));
        Customer customer = customers.findById(requiredId(reservation.getCustomerId(), "Select a customer."));
        requireActive(customer);
        reservations.lockRoom(room.getId());
        validateDates(reservation);
        validateRoomCanBeReserved(room);
        if (reservations.hasOverlap(room.getId(), reservation.getCheckIn(), reservation.getCheckOut(), id)) {
            throw new IllegalArgumentException("The selected room already has an overlapping active booking.");
        }
        reservation.setId(id);
        reservation.setCustomerName(customer.getFullName());
        reservation.setPhone(customer.getMobileNumber());
        reservation.setRoomRate(room.getPricePerNight());
        calculateCharges(reservation);
        if (reservations.update(reservation) != 1) {
            throw new IllegalArgumentException("Booking is no longer editable.");
        }
        if (!existing.getRoomId().equals(room.getId())
                && !reservations.hasOtherActiveReservation(existing.getRoomId(), id)) {
            reservations.setRoomStatus(existing.getRoomId(), "AVAILABLE");
        }
        if (!"OCCUPIED".equals(room.getStatus())) {
            reservations.setRoomStatus(room.getId(), "RESERVED");
        }
    }

    @Transactional
    public void changeStatus(long id, String action) {
        reservations.lockReservation(id);
        Reservation reservation = findById(id);
        String target = switch (action == null ? "" : action) {
            case "check-in" -> {
                if (!"RESERVED".equals(reservation.getStatus())) {
                    throw new IllegalArgumentException("Only reserved bookings can be checked in.");
                }
                yield "CHECKED_IN";
            }
            case "check-out" -> {
                if (!"CHECKED_IN".equals(reservation.getStatus())) {
                    throw new IllegalArgumentException("Only checked-in bookings can be checked out.");
                }
                yield "CHECKED_OUT";
            }
            case "cancel" -> {
                if (!"RESERVED".equals(reservation.getStatus())) {
                    throw new IllegalArgumentException("Only reserved bookings can be cancelled.");
                }
                yield "CANCELLED";
            }
            default -> throw new IllegalArgumentException("Unknown booking action.");
        };
        reservations.setStatus(id, target);
        if ("CHECKED_IN".equals(target)) {
            reservations.setRoomStatus(reservation.getRoomId(), "OCCUPIED");
        } else if ("CHECKED_OUT".equals(target) || "CANCELLED".equals(target)) {
            reservations.setRoomStatus(reservation.getRoomId(),
                    reservations.hasOtherActiveReservation(reservation.getRoomId(), id) ? "RESERVED" : "AVAILABLE");
        }
    }

    public int countActive() {
        return reservations.countActive();
    }

    private void validateRoomCanBeReserved(Room room) {
        if ("MAINTENANCE".equals(room.getStatus())) {
            throw new IllegalArgumentException("Rooms under maintenance cannot be booked.");
        }
        if ("OCCUPIED".equals(room.getStatus())) {
            throw new IllegalArgumentException("Occupied rooms cannot be booked.");
        }
    }

    private void requireActive(Customer customer) {
        if (!"ACTIVE".equals(customer.getStatus())) {
            throw new IllegalArgumentException("Only active customers can make bookings.");
        }
    }

    private void validateDates(Reservation reservation) {
        if (reservation.getCheckIn() == null || reservation.getCheckOut() == null
                || !reservation.getCheckOut().isAfter(reservation.getCheckIn())) {
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }
        long nights = ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut());
        if (nights < 1 || nights > 365) {
            throw new IllegalArgumentException("A booking must be between 1 and 365 nights.");
        }
        reservation.setNights((int) nights);
    }

    private void calculateCharges(Reservation reservation) {
        reservation.setTotalRoomCharges(reservation.getRoomRate()
                .multiply(BigDecimal.valueOf(reservation.getNights())).setScale(2));
    }

    private long requiredId(Long id, String message) {
        if (id == null || id < 1) {
            throw new IllegalArgumentException(message);
        }
        return id;
    }
}
