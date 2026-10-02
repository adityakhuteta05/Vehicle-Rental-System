-- =========================================================================
-- DriveSense: Smart Car Rental Platform
-- Relational Database Schema Definition (MySQL 8 / H2 MySQL Mode compatible)
-- =========================================================================

-- Drop tables if exists in reverse dependency order
DROP TABLE IF EXISTS trust_events;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS condition_reports;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS booking_extras;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS car_features;
DROP TABLE IF EXISTS cars;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS users;

-- 1. Users table (Customer / Admin with Trust Score and Licence)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(15),
    password_hash VARCHAR(100) NOT NULL,
    licence_no VARCHAR(30),
    dob DATE,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_CUSTOMER',
    trust_score INT NOT NULL DEFAULT 50,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
);

-- 2. Locations table (Airport, Flagship Hub, Station)
CREATE TABLE locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(60) NOT NULL,
    address VARCHAR(255)
);

-- 3. Cars table (Specifications, classification, pricing rates, carbon metric)
CREATE TABLE cars (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand VARCHAR(60) NOT NULL,
    model VARCHAR(60) NOT NULL,
    reg_number VARCHAR(25) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL, -- HATCHBACK, SEDAN, SUV, LUXURY, EV
    fuel_type VARCHAR(15) NOT NULL, -- PETROL, DIESEL, CNG, EV
    transmission VARCHAR(15) NOT NULL, -- MANUAL, AUTOMATIC
    seats INT NOT NULL,
    boot_size VARCHAR(5) NOT NULL, -- S, M, L
    price_per_day DECIMAL(10,2) NOT NULL,
    price_per_hour DECIMAL(10,2) NOT NULL,
    km_per_day_limit INT NOT NULL DEFAULT 300,
    extra_km_rate DECIMAL(6,2) NOT NULL DEFAULT 12.00,
    co2_g_per_km INT NOT NULL DEFAULT 140,
    image_url VARCHAR(500),
    status VARCHAR(15) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, MAINTENANCE, RETIRED
    avg_rating DECIMAL(2,1) DEFAULT 4.8,
    INDEX idx_car_reg (reg_number),
    INDEX idx_car_type (type),
    INDEX idx_car_status (status)
);

-- 4. Car Features mapping
CREATE TABLE car_features (
    car_id BIGINT NOT NULL,
    feature VARCHAR(100) NOT NULL,
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
);

-- 5. Bookings table (Concurrency safe, pricing breakdown, carbon estimation)
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_code VARCHAR(30) NOT NULL UNIQUE, -- DS-2026-000123
    user_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    pickup_location_id BIGINT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    actual_return_time DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED
    trip_type VARCHAR(30),
    base_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    surcharge_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    deposit_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    extra_charges DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    est_distance_km INT,
    est_co2_kg DECIMAL(6,2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (car_id) REFERENCES cars(id),
    FOREIGN KEY (pickup_location_id) REFERENCES locations(id),
    INDEX idx_booking_car_dates (car_id, start_time, end_time),
    INDEX idx_booking_code (booking_code),
    CONSTRAINT chk_dates CHECK (end_time > start_time)
);

-- 6. Booking Extras table (Driver, Insurance, Child seat)
CREATE TABLE booking_extras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    extra_name VARCHAR(50) NOT NULL,
    price DECIMAL(8,2) NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- 7. Payments table (Simulated gateway audit)
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    method VARCHAR(30) NOT NULL, -- CARD, UPI
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED
    txn_ref VARCHAR(60) NOT NULL,
    paid_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- 8. Digital Condition Reports (DCR) (Pickup vs Return inspections)
CREATE TABLE condition_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    stage VARCHAR(15) NOT NULL, -- PICKUP, RETURN
    fuel_percent INT NOT NULL DEFAULT 100,
    odometer_km INT NOT NULL DEFAULT 0,
    damage_zones VARCHAR(255) DEFAULT '', -- e.g. FRONT,LEFT
    notes TEXT,
    photo_urls TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    INDEX idx_report_booking_stage (booking_id, stage)
);

-- 9. Verified Customer Reviews table
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (car_id) REFERENCES cars(id)
);

-- 10. Customer Trust Score Audit Events table
CREATE TABLE trust_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reason VARCHAR(150) NOT NULL,
    delta INT NOT NULL,
    resulting_score INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_trust_event_user (user_id)
);
