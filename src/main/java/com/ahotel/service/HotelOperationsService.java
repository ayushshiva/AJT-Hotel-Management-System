package com.ahotel.service;

import com.ahotel.config.BillingProperties;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class HotelOperationsService {
    private static final Set<String> PAYMENT_METHODS = Set.of("CASH", "UPI", "CARD");
    private static final Set<String> EMPLOYEE_DESIGNATIONS = Set.of("Manager", "Receptionist", "Accountant",
            "Housekeeping", "Room Service Staff", "Kitchen Staff", "Laundry Staff");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern MOBILE = Pattern.compile("^[+()0-9 .-]{7,20}$");

    private final JdbcTemplate db;
    private final BigDecimal gstRate;

    public HotelOperationsService(JdbcTemplate db, BillingProperties billingProperties) {
        this.db = db;
        this.gstRate = billingProperties.getGstRate();
    }

    public List<Map<String, Object>> foodItems(String search, String category) {
        return rows("SELECT id,name,category,description,price,available,created_at FROM food_items "
                + "WHERE (?='' OR LOWER(name) LIKE ? OR LOWER(category) LIKE ?) AND (?='' OR category=?) ORDER BY category,name",
                search == null ? "" : search.trim(),
                search == null || search.isBlank() ? "%" : "%" + search.trim().toLowerCase(Locale.ROOT) + "%",
                search == null || search.isBlank() ? "%" : "%" + search.trim().toLowerCase(Locale.ROOT) + "%",
                category == null ? "" : category, category == null ? "" : category);
    }

    public List<Map<String, Object>> foodCategories() {
        return rows("SELECT DISTINCT category FROM food_items ORDER BY category");
    }

    @Transactional
    public void saveFoodItem(Long id, String name, String category, String description, BigDecimal price, boolean available) {
        name = requiredText(name, "Food name", 120);
        category = requiredText(category, "Category", 60);
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0 || price.scale() > 2) {
            throw new IllegalArgumentException("Food price must be positive with at most two decimal places.");
        }
        try {
            if (id == null) {
                insert("INSERT INTO food_items(name,category,description,price,available) VALUES(?,?,?,?,?)",
                        name, category, trim(description), price, available);
            } else if (db.update("UPDATE food_items SET name=?,category=?,description=?,price=?,available=? WHERE id=?",
                    name, category, trim(description), price, available, id) != 1) {
                throw new IllegalArgumentException("Food item not found.");
            }
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Food name already exists or the item is used by order history.", e);
        }
    }

    @Transactional
    public void deleteFoodItem(long id) {
        try {
            if (db.update("DELETE FROM food_items WHERE id=?", id) != 1) {
                throw new IllegalArgumentException("Food item not found.");
            }
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Food items with order history cannot be deleted.", e);
        }
    }

    public List<Map<String, Object>> foodOrders() {
        return rows("SELECT o.id,o.room_number,o.total,o.status,o.ordered_at,c.full_name customer_name "
                + "FROM food_orders o LEFT JOIN customers c ON c.id=o.customer_id ORDER BY o.ordered_at DESC");
    }

    public List<Map<String, Object>> foodOrderLines() {
        return rows("SELECT order_id,item_name,quantity,unit_price,line_total FROM food_order_items ORDER BY order_id,id");
    }

    public List<Map<String, Object>> checkedInBookings() {
        return rows("SELECT r.id,r.customer_name,rm.room_number,r.check_in,r.check_out FROM reservations r "
                + "JOIN rooms rm ON rm.id=r.room_id WHERE r.status='CHECKED_IN' ORDER BY rm.room_number");
    }

    @Transactional
    public long placeFoodOrder(long reservationId, Map<Long, Integer> quantities) {
        Map<String, Object> reservation = one("SELECT r.id,r.customer_id,r.room_id,rm.room_number,r.status "
                + "FROM reservations r JOIN rooms rm ON rm.id=r.room_id WHERE r.id=?", reservationId);
        if (!"CHECKED_IN".equals(reservation.get("status"))) {
            throw new IllegalArgumentException("Food orders require a checked-in booking.");
        }
        if (quantities == null || quantities.isEmpty()) {
            throw new IllegalArgumentException("Choose at least one menu item.");
        }
        BigDecimal total = BigDecimal.ZERO;
        List<Map<String, Object>> lines = new ArrayList<>();
        for (Map.Entry<Long, Integer> item : quantities.entrySet()) {
            if (item.getValue() == null || item.getValue() < 1 || item.getValue() > 100) {
                throw new IllegalArgumentException("Each food quantity must be between 1 and 100.");
            }
            Map<String, Object> food = one("SELECT id,name,price FROM food_items WHERE id=? AND available=TRUE", item.getKey());
            BigDecimal price = (BigDecimal) food.get("price");
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(item.getValue())).setScale(2, RoundingMode.HALF_UP);
            total = total.add(lineTotal);
            lines.add(Map.of("foodId", item.getKey(), "name", food.get("name"),
                    "quantity", item.getValue(), "price", price, "lineTotal", lineTotal));
        }
        long orderId = insert("INSERT INTO food_orders(customer_id,reservation_id,room_number,total,status) "
                + "VALUES(?,?,?,?,'PENDING')", reservation.get("customer_id"), reservationId,
                reservation.get("room_number"), total);
        for (Map<String, Object> line : lines) {
            insert("INSERT INTO food_order_items(order_id,food_item_id,item_name,quantity,unit_price,line_total) "
                            + "VALUES(?,?,?,?,?,?)",
                    orderId, line.get("foodId"), line.get("name"), line.get("quantity"),
                    line.get("price"), line.get("lineTotal"));
        }
        return orderId;
    }

    @Transactional
    public void changeFoodOrderStatus(long id, String next) {
        transition("food_orders", id, next, Map.of(
                "PENDING", Set.of("PREPARING", "CANCELLED"),
                "PREPARING", Set.of("READY", "CANCELLED"),
                "READY", Set.of("SERVED"),
                "SERVED", Set.of(),
                "CANCELLED", Set.of()));
    }

    public List<Map<String, Object>> laundryRequests(String search) {
        String pattern = "%" + (search == null ? "" : search.trim().toLowerCase(Locale.ROOT)) + "%";
        return rows("SELECT l.id,l.item_name,l.quantity,l.rate_per_item,l.total_amount,l.request_date,"
                        + "l.expected_delivery_date,l.status,l.room_number,c.full_name customer_name "
                        + "FROM laundry_requests l JOIN customers c ON c.id=l.customer_id "
                        + "WHERE ?='' OR LOWER(c.full_name) LIKE ? OR LOWER(COALESCE(l.room_number,'')) LIKE ? "
                        + "ORDER BY l.request_date DESC",
                search == null ? "" : search.trim(), pattern, pattern);
    }

    @Transactional
    public long createLaundry(long reservationId, String itemName, int quantity,
                              BigDecimal rate, LocalDate expectedDelivery) {
        Map<String, Object> r = checkedInReservation(reservationId);
        String item = requiredText(itemName, "Laundry item", 120);
        if (quantity < 1 || quantity > 500 || rate == null || rate.signum() < 0 || rate.scale() > 2) {
            throw new IllegalArgumentException("Laundry quantity must be 1–500 and rate must be zero or greater.");
        }
        BigDecimal total = rate.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        return insert("INSERT INTO laundry_requests(customer_id,reservation_id,room_number,item_name,quantity,"
                        + "rate_per_item,total_amount,expected_delivery_date,status) VALUES(?,?,?,?,?,?,?,?, 'RECEIVED')",
                r.get("customer_id"), reservationId, r.get("room_number"), item, quantity, rate, total, expectedDelivery);
    }

    @Transactional
    public void updateLaundry(long id, long reservationId, String itemName, int quantity,
                              BigDecimal rate, LocalDate expectedDelivery) {
        Map<String, Object> existing = one("SELECT status FROM laundry_requests WHERE id=?", id);
        if (!"RECEIVED".equals(existing.get("status"))) {
            throw new IllegalArgumentException("Only received laundry requests can be edited.");
        }
        Map<String, Object> reservation = checkedInReservation(reservationId);
        String item = requiredText(itemName, "Laundry item", 120);
        if (quantity < 1 || quantity > 500 || rate == null || rate.signum() < 0 || rate.scale() > 2) {
            throw new IllegalArgumentException("Laundry quantity must be 1–500 and rate must be zero or greater.");
        }
        BigDecimal total = rate.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        if (db.update("UPDATE laundry_requests SET customer_id=?,reservation_id=?,room_number=?,item_name=?,"
                        + "quantity=?,rate_per_item=?,total_amount=?,expected_delivery_date=? WHERE id=? AND status='RECEIVED'",
                reservation.get("customer_id"), reservationId, reservation.get("room_number"), item,
                quantity, rate, total, expectedDelivery, id) != 1) {
            throw new IllegalArgumentException("Laundry request is no longer editable.");
        }
    }

    @Transactional
    public void changeLaundryStatus(long id, String next) {
        transition("laundry_requests", id, next, Map.of(
                "RECEIVED", Set.of("IN_PROGRESS", "CANCELLED"),
                "IN_PROGRESS", Set.of("READY", "CANCELLED"),
                "READY", Set.of("DELIVERED"),
                "DELIVERED", Set.of(),
                "CANCELLED", Set.of()));
    }

    public List<Map<String, Object>> serviceRequests() {
        return rows("SELECT s.id,s.room_number,s.service_type,s.description,s.service_charge,s.request_date,"
                + "s.completion_date,s.status,c.full_name customer_name,e.full_name employee_name "
                + "FROM room_service_requests s JOIN customers c ON c.id=s.customer_id "
                + "LEFT JOIN employees e ON e.id=s.assigned_employee_id ORDER BY s.request_date DESC");
    }

    @Transactional
    public long createServiceRequest(long reservationId, String serviceType, String description, BigDecimal charge) {
        Map<String, Object> r = checkedInReservation(reservationId);
        String type = requiredText(serviceType, "Service type", 80);
        if (charge == null || charge.signum() < 0 || charge.scale() > 2) {
            throw new IllegalArgumentException("Service charge must be zero or greater with at most two decimals.");
        }
        return insert("INSERT INTO room_service_requests(customer_id,reservation_id,room_number,service_type,"
                        + "description,service_charge,status) VALUES(?,?,?,?,?,?,'REQUESTED')",
                r.get("customer_id"), reservationId, r.get("room_number"), type, trim(description), charge);
    }

    @Transactional
    public void updateServiceRequest(long id, long reservationId, String serviceType,
                                     String description, BigDecimal charge) {
        Map<String, Object> existing = one("SELECT status FROM room_service_requests WHERE id=?", id);
        if (!"REQUESTED".equals(existing.get("status"))) {
            throw new IllegalArgumentException("Only requested services can be edited.");
        }
        Map<String, Object> reservation = checkedInReservation(reservationId);
        String type = requiredText(serviceType, "Service type", 80);
        if (charge == null || charge.signum() < 0 || charge.scale() > 2) {
            throw new IllegalArgumentException("Service charge must be zero or greater with at most two decimals.");
        }
        if (db.update("UPDATE room_service_requests SET customer_id=?,reservation_id=?,room_number=?,service_type=?,"
                        + "description=?,service_charge=? WHERE id=? AND status='REQUESTED'",
                reservation.get("customer_id"), reservationId, reservation.get("room_number"),
                type, trim(description), charge, id) != 1) {
            throw new IllegalArgumentException("Service request is no longer editable.");
        }
    }

    @Transactional
    public void assignServiceEmployee(long requestId, Long employeeId) {
        if (employeeId == null || employeeId < 1) {
            throw new IllegalArgumentException("Select an employee.");
        }
        Map<String, Object> request = one("SELECT status FROM room_service_requests WHERE id=?", requestId);
        if (!"REQUESTED".equals(request.get("status"))) {
            throw new IllegalArgumentException("Only requested services can be assigned.");
        }
        Integer active = db.queryForObject("SELECT COUNT(*) FROM employees WHERE id=? AND status='ACTIVE'",
                Integer.class, employeeId);
        if (active == null || active == 0) {
            throw new IllegalArgumentException("Select an active employee.");
        }
        db.update("UPDATE room_service_requests SET assigned_employee_id=?,status='ASSIGNED' WHERE id=?",
                employeeId, requestId);
    }

    @Transactional
    public void changeServiceStatus(long id, String next) {
        transition("room_service_requests", id, next, Map.of(
                "REQUESTED", Set.of("CANCELLED"),
                "ASSIGNED", Set.of("IN_PROGRESS", "CANCELLED"),
                "IN_PROGRESS", Set.of("COMPLETED", "CANCELLED"),
                "COMPLETED", Set.of(),
                "CANCELLED", Set.of()));
        if ("COMPLETED".equals(next)) {
            db.update("UPDATE room_service_requests SET completion_date=CURRENT_TIMESTAMP WHERE id=?", id);
        }
    }

    public List<Map<String, Object>> employees(String search, String designation) {
        String pattern = "%" + (search == null ? "" : search.trim().toLowerCase(Locale.ROOT)) + "%";
        return rows("SELECT * FROM employees WHERE (?='' OR LOWER(full_name) LIKE ? OR LOWER(mobile_number) LIKE ? "
                        + "OR LOWER(email) LIKE ?) AND (?='' OR designation=?) ORDER BY full_name",
                search == null ? "" : search.trim(), pattern, pattern, pattern,
                designation == null ? "" : designation, designation == null ? "" : designation);
    }

    @Transactional
    public void saveEmployee(Long id, String name, String mobile, String email, String address,
                             String designation, String department, BigDecimal salary, LocalDate joiningDate) {
        name = requiredText(name, "Employee name", 120);
        mobile = requiredText(mobile, "Mobile number", 20);
        email = requiredText(email, "Email", 254).toLowerCase(Locale.ROOT);
        designation = requiredText(designation, "Designation", 40);
        department = requiredText(department, "Department", 60);
        if (!EMPLOYEE_DESIGNATIONS.contains(designation)) {
            throw new IllegalArgumentException("Select a valid employee designation.");
        }
        if (!MOBILE.matcher(mobile).matches() || !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Enter a valid mobile number and email.");
        }
        if (salary == null || salary.signum() < 0 || salary.scale() > 2 || joiningDate == null) {
            throw new IllegalArgumentException("Salary must be non-negative and joining date is required.");
        }
        try {
            if (id == null) {
                insert("INSERT INTO employees(full_name,mobile_number,email,address,designation,department,salary,joining_date,status) "
                                + "VALUES(?,?,?,?,?,?,?,?, 'ACTIVE')",
                        name, mobile, email, trim(address), designation, department, salary, joiningDate);
            } else if (db.update("UPDATE employees SET full_name=?,mobile_number=?,email=?,address=?,designation=?,"
                            + "department=?,salary=?,joining_date=? WHERE id=?",
                    name, mobile, email, trim(address), designation, department, salary, joiningDate, id) != 1) {
                throw new IllegalArgumentException("Employee not found.");
            }
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Mobile number or email is already in use.", e);
        }
    }

    @Transactional
    public void deactivateEmployee(long id) {
        if (db.update("UPDATE employees SET status='INACTIVE' WHERE id=? AND status='ACTIVE'", id) != 1) {
            throw new IllegalArgumentException("Active employee not found.");
        }
    }

    public List<Map<String, Object>> invoices() {
        return rows("SELECT i.*,c.full_name customer_name,rm.room_number FROM invoices i "
                + "JOIN customers c ON c.id=i.customer_id JOIN reservations r ON r.id=i.reservation_id "
                + "JOIN rooms rm ON rm.id=r.room_id "
                + "ORDER BY i.invoice_date DESC");
    }

    public List<Map<String, Object>> completedBookingsWithoutInvoice() {
        return rows("SELECT r.id,r.customer_name,rm.room_number,r.total_room_charges FROM reservations r "
                + "JOIN rooms rm ON rm.id=r.room_id LEFT JOIN invoices i ON i.reservation_id=r.id "
                + "WHERE r.status='CHECKED_OUT' AND i.id IS NULL "
                + "ORDER BY r.booking_date DESC");
    }

    @Transactional
    public long createInvoice(long reservationId, String paymentMethod) {
        paymentMethod = normalizePaymentMethod(paymentMethod);
        Map<String, Object> r = one("SELECT id,customer_id,total_room_charges,status FROM reservations WHERE id=?",
                reservationId);
        if (!"CHECKED_OUT".equals(r.get("status"))) {
            throw new IllegalArgumentException("Invoices can be generated only for checked-out bookings.");
        }
        BigDecimal room = (BigDecimal) r.get("total_room_charges");
        BigDecimal food = sum("SELECT COALESCE(SUM(total),0) FROM food_orders WHERE reservation_id=? AND status='SERVED'",
                reservationId);
        BigDecimal laundry = sum("SELECT COALESCE(SUM(total_amount),0) FROM laundry_requests WHERE reservation_id=? AND status='DELIVERED'",
                reservationId);
        BigDecimal service = sum("SELECT COALESCE(SUM(service_charge),0) FROM room_service_requests "
                + "WHERE reservation_id=? AND status='COMPLETED'", reservationId);
        BigDecimal subtotal = room.add(food).add(laundry).add(service).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(gstRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = subtotal.add(tax);
        String invoiceNumber = "INV-" + LocalDate.now().toString().replace("-", "") + "-" + System.nanoTime();
        try {
            return insert("INSERT INTO invoices(invoice_number,customer_id,reservation_id,room_charges,food_charges,"
                            + "laundry_charges,room_service_charges,subtotal,gst_rate,gst_amount,grand_total,payment_method,payment_status) "
                            + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,'PENDING')",
                    invoiceNumber, r.get("customer_id"), reservationId, room, food, laundry, service, subtotal,
                    gstRate, tax, grandTotal, paymentMethod);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("An invoice already exists for this booking.", e);
        }
    }

    @Transactional
    public void recordPayment(long invoiceId, BigDecimal amount, String method) {
        method = normalizePaymentMethod(method);
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2) {
            throw new IllegalArgumentException("Payment amount must be positive with at most two decimals.");
        }
        Map<String, Object> invoice = one("SELECT grand_total FROM invoices WHERE id=? FOR UPDATE", invoiceId);
        BigDecimal paid = sum("SELECT COALESCE(SUM(amount),0) FROM payments WHERE invoice_id=?", invoiceId);
        if (paid.add(amount).compareTo((BigDecimal) invoice.get("grand_total")) > 0) {
            throw new IllegalArgumentException("Payment exceeds the invoice outstanding balance.");
        }
        insert("INSERT INTO payments(invoice_id,amount,payment_method) VALUES(?,?,?)", invoiceId, amount, method);
        BigDecimal newPaid = paid.add(amount);
        String status = newPaid.compareTo((BigDecimal) invoice.get("grand_total")) == 0
                ? "PAID" : "PARTIALLY_PAID";
        db.update("UPDATE invoices SET payment_status=? WHERE id=?", status, invoiceId);
    }

    public List<Map<String, Object>> invoicePayments(long invoiceId) {
        return rows("SELECT id,amount,payment_method,recorded_at FROM payments WHERE invoice_id=? ORDER BY recorded_at",
                invoiceId);
    }

    public Map<String, Object> invoice(long id) {
        return one("SELECT i.*,c.full_name customer_name,c.mobile_number,rm.room_number,r.check_in,r.check_out,"
                + "COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.invoice_id=i.id),0) paid_amount,"
                + "(i.grand_total-COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.invoice_id=i.id),0)) outstanding_balance "
                + "FROM invoices i JOIN customers c ON c.id=i.customer_id "
                + "JOIN reservations r ON r.id=i.reservation_id JOIN rooms rm ON rm.id=r.room_id WHERE i.id=?", id);
    }

    public List<Map<String, Object>> report(String name, LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new IllegalArgumentException("End date must be on or after start date.");
        }
        String start = from == null ? "" : from.toString();
        String end = to == null ? "" : to.toString();
        String sql = switch (name == null ? "" : name) {
            case "customers" -> "SELECT id,full_name,mobile_number,email_address,status,registration_date report_date "
                    + "FROM customers WHERE (?='' OR registration_date>=?) AND (?='' OR registration_date<=?) ORDER BY registration_date DESC";
            case "rooms" -> "SELECT id,room_number,room_type,status,price_per_night,created_at report_date FROM rooms "
                    + "WHERE (?='' OR CAST(created_at AS DATE)>=?) AND (?='' OR CAST(created_at AS DATE)<=?) ORDER BY room_number";
            case "bookings" -> "SELECT id,customer_name,phone,room_number,check_in,check_out,status,total_room_charges "
                    + "FROM reservations WHERE (?='' OR check_in>=?) AND (?='' OR check_in<=?) ORDER BY check_in DESC";
            case "check-ins" -> "SELECT id,customer_name,phone,room_number,check_in,status FROM reservations "
                    + "WHERE status='CHECKED_IN' AND (?='' OR check_in>=?) AND (?='' OR check_in<=?) ORDER BY check_in DESC";
            case "check-outs" -> "SELECT id,customer_name,phone,room_number,check_out,status FROM reservations "
                    + "WHERE status='CHECKED_OUT' AND (?='' OR check_out>=?) AND (?='' OR check_out<=?) ORDER BY check_out DESC";
            case "food" -> "SELECT o.id,o.room_number,o.total,o.status,o.ordered_at report_date,c.full_name customer_name "
                    + "FROM food_orders o LEFT JOIN customers c ON c.id=o.customer_id WHERE (?='' OR CAST(o.ordered_at AS DATE)>=?) "
                    + "AND (?='' OR CAST(o.ordered_at AS DATE)<=?) ORDER BY o.ordered_at DESC";
            case "laundry" -> "SELECT l.id,l.item_name,l.quantity,l.total_amount,l.status,l.request_date report_date,c.full_name customer_name "
                    + "FROM laundry_requests l JOIN customers c ON c.id=l.customer_id WHERE (?='' OR CAST(l.request_date AS DATE)>=?) "
                    + "AND (?='' OR CAST(l.request_date AS DATE)<=?) ORDER BY l.request_date DESC";
            case "room-service" -> "SELECT s.id,s.service_type,s.service_charge,s.status,s.request_date report_date,c.full_name customer_name "
                    + "FROM room_service_requests s JOIN customers c ON c.id=s.customer_id WHERE (?='' OR CAST(s.request_date AS DATE)>=?) "
                    + "AND (?='' OR CAST(s.request_date AS DATE)<=?) ORDER BY s.request_date DESC";
            case "employees" -> "SELECT id,full_name,mobile_number,email,designation,department,salary,status,joining_date report_date "
                    + "FROM employees WHERE (?='' OR joining_date>=?) AND (?='' OR joining_date<=?) ORDER BY full_name";
            case "revenue", "billing" -> "SELECT i.id,i.invoice_number,c.full_name customer_name,rm.room_number,"
                    + "i.subtotal,i.gst_amount,i.grand_total,i.payment_status,i.invoice_date report_date,"
                    + "COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.invoice_id=i.id),0) paid_amount "
                    + "FROM invoices i JOIN customers c ON c.id=i.customer_id JOIN reservations r ON r.id=i.reservation_id "
                    + "JOIN rooms rm ON rm.id=r.room_id "
                    + "WHERE (?='' OR CAST(i.invoice_date AS DATE)>=?) AND (?='' OR CAST(i.invoice_date AS DATE)<=?) "
                    + "ORDER BY i.invoice_date DESC";
            default -> throw new IllegalArgumentException("Select a valid report.");
        };
        return rows(sql, start, start, end, end);
    }

    private Map<String, Object> checkedInReservation(long id) {
        Map<String, Object> r = one("SELECT r.id,r.customer_id,r.room_id,rm.room_number,r.status "
                + "FROM reservations r JOIN rooms rm ON rm.id=r.room_id WHERE r.id=?", id);
        if (!"CHECKED_IN".equals(r.get("status"))) {
            throw new IllegalArgumentException("This service requires a checked-in booking.");
        }
        return r;
    }

    private void transition(String table, long id, String next, Map<String, Set<String>> allowed) {
        Set<String> valid = Set.of("food_orders", "laundry_requests", "room_service_requests");
        if (!valid.contains(table)) throw new IllegalArgumentException("Invalid status target.");
        Map<String, Object> row = one("SELECT status FROM " + table + " WHERE id=?", id);
        String current = String.valueOf(row.get("status"));
        if (!allowed.getOrDefault(current, Set.of()).contains(next)) {
            throw new IllegalArgumentException("Cannot change status from " + current + " to " + next + ".");
        }
        db.update("UPDATE " + table + " SET status=? WHERE id=?", next, id);
    }

    private String normalizePaymentMethod(String method) {
        String normalized = method == null ? "" : method.trim().toUpperCase(Locale.ROOT);
        if (!PAYMENT_METHODS.contains(normalized)) {
            throw new IllegalArgumentException("Select CASH, UPI or CARD.");
        }
        return normalized;
    }

    private BigDecimal sum(String sql, Object... args) {
        BigDecimal value = db.queryForObject(sql, BigDecimal.class, args);
        return value == null ? BigDecimal.ZERO : value;
    }

    private String requiredText(String value, String field, int max) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > max) {
            throw new IllegalArgumentException(field + " is required and must be " + max + " characters or fewer.");
        }
        return normalized;
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private long insert(String sql, Object... args) {
        KeyHolder key = new GeneratedKeyHolder();
        db.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            return statement;
        }, key);
        Number id = key.getKey();
        if (id == null) throw new IllegalStateException("The database did not return a record ID.");
        return id.longValue();
    }

    private Map<String, Object> one(String sql, Object... args) {
        return rows(sql, args).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The referenced record was not found."));
    }

    private List<Map<String, Object>> rows(String sql, Object... args) {
        return db.query(sql, rs -> {
            List<Map<String, Object>> result = new ArrayList<>();
            var metadata = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new java.util.LinkedHashMap<>();
                for (int i = 1; i <= metadata.getColumnCount(); i++) {
                    row.put(metadata.getColumnLabel(i).toLowerCase(Locale.ROOT), rs.getObject(i));
                }
                result.add(row);
            }
            return result;
        }, args);
    }
}
