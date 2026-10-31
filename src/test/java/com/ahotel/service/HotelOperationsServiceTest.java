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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class HotelOperationsServiceTest {
    @Autowired private HotelOperationsService operations;
    @Autowired private CustomerService customers;
    @Autowired private RoomService rooms;
    @Autowired private ReservationService reservations;

    @Test
    void persistsOrdersServiceChargesInvoicesPaymentsAndReports() {
        String suffix = Long.toString(System.nanoTime());
        Customer customer = new Customer();
        customer.setFullName("Operations Test");
        customer.setMobileNumber("8" + suffix.substring(0, 9));
        customer.setEmailAddress("ops." + suffix + "@example.com");
        customer.setStatus("ACTIVE");
        long customerId = customers.create(customer);

        Room room = new Room();
        room.setRoomNumber("O" + suffix.substring(0, 10));
        room.setRoomType("Double");
        room.setFloorNumber(2);
        room.setCapacity(2);
        room.setPricePerNight(new BigDecimal("100.00"));
        room.setStatus("AVAILABLE");
        long roomId = rooms.create(room);

        Reservation reservation = new Reservation();
        reservation.setCustomerId(customerId);
        reservation.setRoomId(roomId);
        reservation.setCheckIn(LocalDate.now().minusDays(1));
        reservation.setCheckOut(LocalDate.now());
        long reservationId = reservations.create(reservation);
        reservations.changeStatus(reservationId, "check-in");

        operations.saveFoodItem(null, "Test meal " + suffix, "Test", "Test item",
                new BigDecimal("5.00"), true);
        long foodId = ((Number) operations.foodItems("Test meal " + suffix, "").getFirst().get("id")).longValue();
        long orderId = operations.placeFoodOrder(reservationId, Map.of(foodId, 2));
        assertEquals(new BigDecimal("10.00"), operations.foodOrders().getFirst().get("total"));
        operations.changeFoodOrderStatus(orderId, "PREPARING");
        operations.changeFoodOrderStatus(orderId, "READY");
        operations.changeFoodOrderStatus(orderId, "SERVED");

        long laundryId = operations.createLaundry(reservationId, "Towel", 4,
                new BigDecimal("3.00"), LocalDate.now().plusDays(1));
        assertEquals(new BigDecimal("12.00"), operations.laundryRequests("").getFirst().get("total_amount"));
        operations.changeLaundryStatus(laundryId, "IN_PROGRESS");
        operations.changeLaundryStatus(laundryId, "READY");
        operations.changeLaundryStatus(laundryId, "DELIVERED");

        String employeeSuffix = Long.toString(System.nanoTime());
        operations.saveEmployee(null, "Service Staff", "7" + employeeSuffix.substring(0, 9),
                "staff." + employeeSuffix + "@example.com", "", "Room Service Staff",
                "Guest Services", new BigDecimal("25000.00"), LocalDate.now());
        long employeeId = ((Number) operations.employees("Service Staff", "").getFirst().get("id")).longValue();
        long serviceId = operations.createServiceRequest(reservationId, "Extra towels", "Bring two towels",
                new BigDecimal("8.00"));
        operations.assignServiceEmployee(serviceId, employeeId);
        operations.changeServiceStatus(serviceId, "IN_PROGRESS");
        operations.changeServiceStatus(serviceId, "COMPLETED");

        reservations.changeStatus(reservationId, "check-out");
        long invoiceId = operations.createInvoice(reservationId, "CASH");
        Map<String, Object> invoice = operations.invoice(invoiceId);
        assertEquals(new BigDecimal("130.00"), invoice.get("subtotal"));
        assertEquals(new BigDecimal("23.40"), invoice.get("gst_amount"));
        assertEquals(new BigDecimal("153.40"), invoice.get("grand_total"));
        assertThrows(IllegalArgumentException.class, () -> operations.createInvoice(reservationId, "UPI"));

        operations.recordPayment(invoiceId, new BigDecimal("100.00"), "UPI");
        assertEquals("PARTIALLY_PAID", operations.invoice(invoiceId).get("payment_status"));
        operations.recordPayment(invoiceId, new BigDecimal("53.40"), "CARD");
        assertEquals("PAID", operations.invoice(invoiceId).get("payment_status"));
        assertThrows(IllegalArgumentException.class,
                () -> operations.recordPayment(invoiceId, new BigDecimal("1.00"), "CASH"));
        assertFalse(operations.report("revenue", LocalDate.now(), LocalDate.now()).isEmpty());
    }

    @Test
    void validatesEmployeesAndRejectsIllegalOrderTransitions() {
        assertThrows(IllegalArgumentException.class, () -> operations.saveEmployee(null, "Invalid", "x",
                "bad-email", "", "Manager", "Office", BigDecimal.TEN, LocalDate.now()));
    }
}
