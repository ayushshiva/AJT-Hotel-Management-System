package com.ahotel.service;

import com.ahotel.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CustomerServiceTest {
    @Autowired
    private CustomerService customers;

    @Autowired
    private JdbcTemplate db;

    @Test
    void createsSearchesAndUpdatesCustomerWithoutChangingRegistrationDate() {
        long id = customers.create(customer("Aman Kumar", "+91 98765-43210", "aman@example.com"));

        Customer created = customers.findById(id);
        assertEquals("ACTIVE", created.getStatus());
        assertEquals(1, customers.count("aman", "ACTIVE"));

        Customer edited = customer("Aman Kumar", "+91 98765-43210", "aman.updated@example.com");
        edited.setStatus("INACTIVE");
        customers.update(id, edited);

        Customer saved = customers.findById(id);
        assertEquals("aman.updated@example.com", saved.getEmailAddress());
        assertEquals(created.getRegistrationDate(), saved.getRegistrationDate());
        assertEquals(1, customers.count("aman.updated@example.com", "INACTIVE"));
        assertEquals(1, customers.count("98765", "INACTIVE"));
    }

    @Test
    void rejectsDuplicateContactInformationAndInvalidEmail() {
        customers.create(customer("Aman Kumar", "9876543210", "aman@example.com"));

        Customer duplicate = customer("Another Guest", "9876543210", "another@example.com");
        assertThrows(IllegalArgumentException.class, () -> customers.create(duplicate));

        Customer invalid = customer("Another Guest", "9876543211", "not-an-email");
        assertThrows(IllegalArgumentException.class, () -> customers.create(invalid));
    }

    @Test
    void deactivatesCustomerWithBookingsAndDeletesCustomerWithoutBookings() {
        long bookedId = customers.create(customer("Booked Guest", "9000000001", "booked@example.com"));
        db.update("INSERT INTO bookings(customer_name, phone, room_number, nights) VALUES(?, ?, ?, ?)",
                "Booked Guest", "9000000001", "301", 2);

        assertFalse(customers.deleteOrDeactivate(bookedId));
        assertEquals("INACTIVE", customers.findById(bookedId).getStatus());
        assertEquals(1, customers.findBookingHistory(bookedId).size());

        long unbookedId = customers.create(customer("New Guest", "9000000002", "new@example.com"));
        assertTrue(customers.deleteOrDeactivate(unbookedId));
        assertThrows(java.util.NoSuchElementException.class, () -> customers.findById(unbookedId));
    }

    private Customer customer(String name, String mobile, String email) {
        Customer customer = new Customer();
        customer.setFullName(name);
        customer.setMobileNumber(mobile);
        customer.setEmailAddress(email);
        customer.setStatus("ACTIVE");
        return customer;
    }
}
