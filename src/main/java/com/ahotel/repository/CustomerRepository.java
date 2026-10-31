package com.ahotel.repository;

import com.ahotel.model.Booking;
import com.ahotel.model.Customer;
import com.ahotel.model.Reservation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepository {
    private static final RowMapper<Customer> CUSTOMER_MAPPER = (rs, row) -> {
        Customer customer = new Customer();
        customer.setId(rs.getLong("id"));
        customer.setFullName(rs.getString("full_name"));
        customer.setMobileNumber(rs.getString("mobile_number"));
        customer.setEmailAddress(rs.getString("email_address"));
        customer.setGender(rs.getString("gender"));
        customer.setAddress(rs.getString("address"));
        customer.setCity(rs.getString("city"));
        customer.setState(rs.getString("state"));
        customer.setCountry(rs.getString("country"));
        customer.setIdProofType(rs.getString("id_proof_type"));
        customer.setIdProofNumber(rs.getString("id_proof_number"));
        customer.setRegistrationDate(rs.getDate("registration_date").toLocalDate());
        customer.setStatus(rs.getString("status"));
        return customer;
    };

    private final JdbcTemplate db;

    public CustomerRepository(JdbcTemplate db) {
        this.db = db;
    }

    public Optional<Customer> findById(long id) {
        return db.query("SELECT * FROM customers WHERE id = ?", CUSTOMER_MAPPER, id)
                .stream().findFirst();
    }

    public List<Customer> findActive() {
        return db.query("SELECT * FROM customers WHERE status = 'ACTIVE' ORDER BY full_name", CUSTOMER_MAPPER);
    }

    public List<Customer> findPage(@Nullable String search, @Nullable String status, int limit, int offset) {
        Query query = customerQuery(search, status);
        query.sql.append(" ORDER BY registration_date DESC, id DESC LIMIT ? OFFSET ?");
        query.parameters.add(limit);
        query.parameters.add(offset);
        String sql = "SELECT * FROM customers" + query.sql;
        return db.query(sql, CUSTOMER_MAPPER, query.parameters.toArray());
    }

    public List<Customer> findRecent(int limit) {
        String sql = "SELECT * FROM customers ORDER BY registration_date DESC, id DESC LIMIT ?";
        return db.query(sql, CUSTOMER_MAPPER, limit);
    }

    public int count(@Nullable String search, @Nullable String status) {
        Query query = customerQuery(search, status);
        Integer count = db.queryForObject("SELECT COUNT(*) FROM customers" + query.sql,
                Integer.class, query.parameters.toArray());
        return count == null ? 0 : count;
    }

    public int countAll() {
        Integer count = db.queryForObject("SELECT COUNT(*) FROM customers", Integer.class);
        return count == null ? 0 : count;
    }

    public int countActive() {
        Integer count = db.queryForObject("SELECT COUNT(*) FROM customers WHERE status = 'ACTIVE'", Integer.class);
        return count == null ? 0 : count;
    }

    public boolean duplicateExists(String mobileNumber, String emailAddress, @Nullable Long exceptId) {
        String sql = "SELECT COUNT(*) FROM customers WHERE (mobile_number = ? OR email_address = ?)"
                + (exceptId == null ? "" : " AND id <> ?");
        Integer count = exceptId == null
                ? db.queryForObject(sql, Integer.class, mobileNumber, emailAddress)
                : db.queryForObject(sql, Integer.class, mobileNumber, emailAddress, exceptId);
        return count != null && count > 0;
    }

    public long insert(Customer customer) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        db.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO customers(full_name, mobile_number, email_address, gender, address, city, state, country, id_proof_type, id_proof_number, registration_date, status) "
                            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_DATE, 'ACTIVE')",
                    new String[]{"id"});
            statement.setString(1, customer.getFullName());
            statement.setString(2, customer.getMobileNumber());
            statement.setString(3, customer.getEmailAddress());
            statement.setString(4, customer.getGender());
            statement.setString(5, customer.getAddress());
            statement.setString(6, customer.getCity());
            statement.setString(7, customer.getState());
            statement.setString(8, customer.getCountry());
            statement.setString(9, customer.getIdProofType());
            statement.setString(10, customer.getIdProofNumber());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("The database did not return a customer ID.");
        }
        return key.longValue();
    }

    public int update(Customer customer) {
        return db.update(
                "UPDATE customers SET full_name = ?, mobile_number = ?, email_address = ?, gender = ?, address = ?, city = ?, state = ?, country = ?, id_proof_type = ?, id_proof_number = ?, status = ? WHERE id = ?",
                customer.getFullName(), customer.getMobileNumber(), customer.getEmailAddress(),
                customer.getGender(), customer.getAddress(), customer.getCity(), customer.getState(),
                customer.getCountry(), customer.getIdProofType(), customer.getIdProofNumber(),
                customer.getStatus(), customer.getId());
    }

    public int countBookingsForPhone(String phone) {
        Integer count = db.queryForObject("SELECT COUNT(*) FROM bookings WHERE phone = ?", Integer.class, phone);
        return count == null ? 0 : count;
    }

    public int countReservationsForCustomer(long customerId) {
        Integer count = db.queryForObject("SELECT COUNT(*) FROM reservations WHERE customer_id = ?",
                Integer.class, customerId);
        return count == null ? 0 : count;
    }

    public List<Reservation> findReservationsForCustomer(long customerId) {
        return db.query("SELECT r.*, rm.room_number FROM reservations r JOIN rooms rm ON rm.id=r.room_id "
                        + "WHERE r.customer_id=? ORDER BY r.booking_date DESC",
                (rs, row) -> {
                    Reservation reservation = new Reservation();
                    reservation.setId(rs.getLong("id"));
                    reservation.setCustomerId(rs.getLong("customer_id"));
                    reservation.setRoomId(rs.getLong("room_id"));
                    reservation.setRoomNumber(rs.getString("room_number"));
                    reservation.setCustomerName(rs.getString("customer_name"));
                    reservation.setPhone(rs.getString("phone"));
                    reservation.setCheckIn(rs.getDate("check_in").toLocalDate());
                    reservation.setCheckOut(rs.getDate("check_out").toLocalDate());
                    reservation.setNights(rs.getInt("nights"));
                    reservation.setTotalRoomCharges(rs.getBigDecimal("total_room_charges"));
                    reservation.setStatus(rs.getString("status"));
                    return reservation;
                }, customerId);
    }

    public List<Booking> findBookingsForPhone(String phone) {
        return db.query(
                "SELECT id, customer_name, phone, room_number, nights FROM bookings WHERE phone = ? ORDER BY id DESC",
                (rs, row) -> new Booking(rs.getLong("id"), rs.getString("customer_name"),
                        rs.getString("phone"), rs.getString("room_number"), rs.getInt("nights")),
                phone);
    }

    public int deactivate(long id) {
        return db.update("UPDATE customers SET status = 'INACTIVE' WHERE id = ?", id);
    }

    public int delete(long id) {
        return db.update("DELETE FROM customers WHERE id = ?", id);
    }

    private Query customerQuery(@Nullable String search, @Nullable String status) {
        StringBuilder sql = new StringBuilder(" WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(full_name) LIKE ? OR LOWER(mobile_number) LIKE ? OR LOWER(email_address) LIKE ?)");
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            parameters.add(status);
        }
        return new Query(sql, parameters);
    }

    private static final class Query {
        private final StringBuilder sql;
        private final List<Object> parameters;

        private Query(StringBuilder sql, List<Object> parameters) {
            this.sql = sql;
            this.parameters = parameters;
        }
    }
}
