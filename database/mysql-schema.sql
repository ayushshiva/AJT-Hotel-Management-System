CREATE DATABASE IF NOT EXISTS hotel_management;
USE hotel_management;

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(120) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    room_number VARCHAR(12) NOT NULL UNIQUE,
    nights INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    mobile_number VARCHAR(20) NOT NULL UNIQUE,
    email_address VARCHAR(254) NOT NULL UNIQUE,
    gender VARCHAR(30),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    id_proof_type VARCHAR(40),
    id_proof_number VARCHAR(80),
    registration_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_customers_status CHECK (status IN ('ACTIVE','INACTIVE'))
);

CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(12) NOT NULL UNIQUE,
    room_type VARCHAR(20) NOT NULL,
    floor_number INT NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    price_per_night DECIMAL(10,2) NOT NULL CHECK (price_per_night > 0),
    description VARCHAR(500),
    amenities VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
        CHECK (status IN ('AVAILABLE','RESERVED','OCCUPIED','MAINTENANCE')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT,
    room_id BIGINT NOT NULL,
    customer_name VARCHAR(120) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    nights INT NOT NULL,
    room_rate DECIMAL(10,2) NOT NULL,
    total_room_charges DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    booking_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservations_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_reservations_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT chk_reservation_dates CHECK (check_out > check_in),
    CONSTRAINT chk_reservation_status CHECK (status IN ('RESERVED','CHECKED_IN','CHECKED_OUT','CANCELLED')),
    INDEX idx_reservations_room_dates (room_id, check_in, check_out, status),
    INDEX idx_reservations_customer (customer_id, booking_date)
);

CREATE TABLE IF NOT EXISTS food_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL UNIQUE,
    category VARCHAR(60) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS food_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT,
    reservation_id BIGINT,
    room_number VARCHAR(12),
    total DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ordered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_food_order_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_food_order_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT chk_food_order_status CHECK (status IN ('PENDING','PREPARING','READY','SERVED','CANCELLED')),
    INDEX idx_food_orders_customer (customer_id, ordered_at)
);

CREATE TABLE IF NOT EXISTS food_order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    food_item_id BIGINT,
    item_name VARCHAR(120) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_food_line_order FOREIGN KEY (order_id) REFERENCES food_orders(id),
    CONSTRAINT fk_food_line_item FOREIGN KEY (food_item_id) REFERENCES food_items(id)
);

CREATE TABLE IF NOT EXISTS employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    mobile_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(254) NOT NULL UNIQUE,
    address VARCHAR(255),
    designation VARCHAR(40) NOT NULL,
    department VARCHAR(60) NOT NULL,
    salary DECIMAL(12,2) NOT NULL CHECK (salary >= 0),
    joining_date DATE NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE'))
);

CREATE TABLE IF NOT EXISTS laundry_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    reservation_id BIGINT,
    room_number VARCHAR(12),
    item_name VARCHAR(120) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    rate_per_item DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    request_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expected_delivery_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    CONSTRAINT fk_laundry_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_laundry_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT chk_laundry_status CHECK (status IN ('RECEIVED','IN_PROGRESS','READY','DELIVERED','CANCELLED')),
    INDEX idx_laundry_customer (customer_id, request_date)
);

CREATE TABLE IF NOT EXISTS room_service_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    reservation_id BIGINT,
    room_number VARCHAR(12),
    service_type VARCHAR(80) NOT NULL,
    description VARCHAR(500),
    assigned_employee_id BIGINT,
    service_charge DECIMAL(10,2) NOT NULL,
    request_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completion_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    CONSTRAINT fk_service_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_service_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_service_employee FOREIGN KEY (assigned_employee_id) REFERENCES employees(id),
    CONSTRAINT chk_service_status CHECK (status IN ('REQUESTED','ASSIGNED','IN_PROGRESS','COMPLETED','CANCELLED')),
    INDEX idx_room_service_customer (customer_id, request_date)
);

CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_number VARCHAR(40) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL UNIQUE,
    room_charges DECIMAL(12,2) NOT NULL,
    food_charges DECIMAL(12,2) NOT NULL,
    laundry_charges DECIMAL(12,2) NOT NULL,
    room_service_charges DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    gst_rate DECIMAL(5,2) NOT NULL,
    gst_amount DECIMAL(12,2) NOT NULL,
    grand_total DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(10) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    invoice_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoice_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_invoice_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT chk_invoice_method CHECK (payment_method IN ('CASH','UPI','CARD')),
    CONSTRAINT chk_invoice_payment_status CHECK (payment_status IN ('PENDING','PARTIALLY_PAID','PAID')),
    INDEX idx_invoices_customer (customer_id, invoice_date)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    payment_method VARCHAR(10) NOT NULL CHECK (payment_method IN ('CASH','UPI','CARD')),
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);