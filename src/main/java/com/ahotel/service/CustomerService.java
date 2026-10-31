package com.ahotel.service;

import com.ahotel.model.Booking;
import com.ahotel.model.Customer;
import com.ahotel.repository.CustomerRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

@Service
public class CustomerService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern MOBILE = Pattern.compile("^[+()0-9 .-]{7,20}$");
    private static final List<String> GENDERS = List.of("Female", "Male", "Non-binary", "Prefer not to say");
    private static final List<String> ID_PROOF_TYPES = List.of("Passport", "National ID", "Driver license", "Other");

    private final CustomerRepository customers;

    public CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public Customer findById(long id) {
        if (id < 1) {
            throw new NoSuchElementException("Customer not found.");
        }
        return customers.findById(id).orElseThrow(() -> new NoSuchElementException("Customer not found."));
    }

    @Transactional(readOnly = true)
    public List<Customer> findPage(String search, String status, int limit, int offset) {
        String normalizedStatus = normalizeStatusFilter(status);
        return customers.findPage(search, normalizedStatus, limit, offset);
    }

    @Transactional(readOnly = true)
    public int count(String search, String status) {
        return customers.count(search, normalizeStatusFilter(status));
    }

    @Transactional(readOnly = true)
    public int countAll() { return customers.countAll(); }

    @Transactional(readOnly = true)
    public int countActive() { return customers.countActive(); }

    @Transactional(readOnly = true)
    public List<Customer> findRecent(int limit) { return customers.findRecent(limit); }

    @Transactional(readOnly = true)
    public List<Booking> findBookingHistory(long customerId) {
        return customers.findBookingsForPhone(findById(customerId).getMobileNumber());
    }

    @Transactional(readOnly = true)
    public List<com.ahotel.model.Reservation> findReservationHistory(long customerId) {
        findById(customerId);
        return customers.findReservationsForCustomer(customerId);
    }

    @Transactional(readOnly = true)
    public int countBookings(long customerId) {
        return customers.countBookingsForPhone(findById(customerId).getMobileNumber());
    }

    @Transactional
    public long create(Customer customer) {
        normalizeAndValidate(customer, true);
        if (customers.duplicateExists(customer.getMobileNumber(), customer.getEmailAddress(), null)) {
            throw new IllegalArgumentException("A customer with this mobile number or email already exists.");
        }
        try {
            return customers.insert(customer);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("A customer with this mobile number or email already exists.", e);
        }
    }

    @Transactional
    public void update(long id, Customer customer) {
        findById(id);
        customer.setId(id);
        normalizeAndValidate(customer, false);
        if (customers.duplicateExists(customer.getMobileNumber(), customer.getEmailAddress(), id)) {
            throw new IllegalArgumentException("A customer with this mobile number or email already exists.");
        }
        try {
            if (customers.update(customer) != 1) {
                throw new NoSuchElementException("Customer not found.");
            }
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("A customer with this mobile number or email already exists.", e);
        }
    }

    @Transactional
    public boolean deleteOrDeactivate(long id) {
        Customer customer = findById(id);
        if (customers.countBookingsForPhone(customer.getMobileNumber()) > 0
                || customers.countReservationsForCustomer(id) > 0) {
            customers.deactivate(id);
            return false;
        }
        if (customers.delete(id) != 1) {
            throw new NoSuchElementException("Customer not found.");
        }
        return true;
    }

    private void normalizeAndValidate(Customer customer, boolean creating) {
        customer.setFullName(trim(customer.getFullName()));
        customer.setMobileNumber(trim(customer.getMobileNumber()));
        customer.setEmailAddress(trim(customer.getEmailAddress()).toLowerCase(Locale.ROOT));
        customer.setGender(emptyToNull(customer.getGender()));
        customer.setAddress(emptyToNull(customer.getAddress()));
        customer.setCity(emptyToNull(customer.getCity()));
        customer.setState(emptyToNull(customer.getState()));
        customer.setCountry(emptyToNull(customer.getCountry()));
        customer.setIdProofType(emptyToNull(customer.getIdProofType()));
        customer.setIdProofNumber(emptyToNull(customer.getIdProofNumber()));

        if (customer.getFullName() == null || customer.getFullName().isBlank()
                || customer.getFullName().length() > 120) {
            throw new IllegalArgumentException("Full name is required and must be 120 characters or fewer.");
        }
        if (customer.getMobileNumber() == null || !MOBILE.matcher(customer.getMobileNumber()).matches()) {
            throw new IllegalArgumentException("Enter a valid mobile number (7–20 characters).");
        }
        if (customer.getEmailAddress() == null || customer.getEmailAddress().length() > 254
                || !EMAIL.matcher(customer.getEmailAddress()).matches()) {
            throw new IllegalArgumentException("Enter a valid email address (254 characters or fewer).");
        }
        validateLength("Address", customer.getAddress(), 255);
        validateLength("City", customer.getCity(), 100);
        validateLength("State", customer.getState(), 100);
        validateLength("Country", customer.getCountry(), 100);
        validateLength("ID proof number", customer.getIdProofNumber(), 80);
        if (customer.getGender() != null && !GENDERS.contains(customer.getGender())) {
            throw new IllegalArgumentException("Select a valid gender.");
        }
        if (customer.getIdProofType() != null && !ID_PROOF_TYPES.contains(customer.getIdProofType())) {
            throw new IllegalArgumentException("Select a valid ID proof type.");
        }
        if ((customer.getIdProofType() == null) != (customer.getIdProofNumber() == null)) {
            throw new IllegalArgumentException("Provide both ID proof type and ID proof number, or leave both blank.");
        }
        if (!creating && !"ACTIVE".equals(customer.getStatus()) && !"INACTIVE".equals(customer.getStatus())) {
            throw new IllegalArgumentException("Select a valid customer status.");
        }
    }

    private String normalizeStatusFilter(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!"ACTIVE".equals(normalized) && !"INACTIVE".equals(normalized)) {
            throw new IllegalArgumentException("Select a valid customer status filter.");
        }
        return normalized;
    }

    private void validateLength(String field, String value, int maximum) {
        if (value != null && value.length() > maximum) {
            throw new IllegalArgumentException(field + " must be " + maximum + " characters or fewer.");
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String emptyToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }
}
