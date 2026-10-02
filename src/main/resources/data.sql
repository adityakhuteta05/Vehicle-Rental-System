-- =========================================================================
-- DriveSense: Demo Data Seeding Script (data.sql)
-- 13 Cars, Locations, Admin & Customer Accounts, Demo Completed & Active Bookings
-- =========================================================================

-- 1. Locations
INSERT INTO locations (id, name, city, address) VALUES
(1, 'Airport T3 Mobility Hub', 'New Delhi', 'Terminal 3 Arrival Lane 4, IGI Airport'),
(2, 'Connaught Place Flagship', 'New Delhi', 'Inner Circle, Block C, CP'),
(3, 'Cyber City Hub', 'Gurugram', 'Building 10 DLF Cyber City'),
(4, 'Koramangala Station', 'Bengaluru', '80 Feet Road, 4th Block'),
(5, 'BKC Executive Lounge', 'Mumbai', 'G Block, Bandra Kurla Complex');

-- 2. Users (Passwords hashed with BCrypt: admin123, user123, vip123)
INSERT INTO users (id, full_name, email, phone, password_hash, licence_no, dob, role, trust_score, created_at) VALUES
(1, 'Karan Mehta (Admin)', 'admin@drivesense.com', '+91 98765 00001', '$2a$10$EIXzaYVK1fsbw1ZfbX3OXePaWxn96p36WQ6G6G6q5oKq1h4m1qR9.', 'DL-ADMIN-9999', '1990-05-12', 'ROLE_ADMIN', 100, '2026-01-01 00:00:00'),
(2, 'Aaditya Sharma', 'user@example.com', '+91 98765 43210', '$2a$10$EIXzaYVK1fsbw1ZfbX3OXePaWxn96p36WQ6G6G6q5oKq1h4m1qR9.', 'DL-042015001234', '1998-08-20', 'ROLE_CUSTOMER', 78, '2026-01-10 10:00:00'),
(3, 'Rohan Verma', 'vip@example.com', '+91 98111 22334', '$2a$10$EIXzaYVK1fsbw1ZfbX3OXePaWxn96p36WQ6G6G6q5oKq1h4m1qR9.', 'DL-012012009876', '1992-03-15', 'ROLE_CUSTOMER', 92, '2026-01-15 12:00:00');

-- 3. Cars
INSERT INTO cars (id, brand, model, reg_number, type, fuel_type, transmission, seats, boot_size, price_per_day, price_per_hour, km_per_day_limit, extra_km_rate, co2_g_per_km, image_url, status, avg_rating) VALUES
(1, 'Hyundai', 'Creta SX(O) Turbo', 'DL04-CC-1024', 'SUV', 'PETROL', 'AUTOMATIC', 5, 'M', 2900.00, 165.00, 300, 12.00, 135, 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 4.8),
(2, 'Mahindra', 'XUV700 AX7 Luxury', 'HR26-DX-7700', 'SUV', 'DIESEL', 'AUTOMATIC', 7, 'L', 4300.00, 240.00, 300, 15.00, 165, 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 4.9),
(3, 'Tesla', 'Model 3 Long Range', 'DL01-EV-3001', 'EV', 'EV', 'AUTOMATIC', 5, 'M', 5900.00, 320.00, 350, 18.00, 0, 'https://images.unsplash.com/photo-1560958089-b8a1929cea89?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 4.9),
(4, 'Hyundai', 'Ioniq 5 Lounge', 'KA05-EV-9999', 'EV', 'EV', 'AUTOMATIC', 5, 'L', 6800.00, 360.00, 350, 20.00, 0, 'https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 4.9),
(5, 'BMW', '530d M-Sport Executive', 'MH01-BM-5050', 'LUXURY', 'DIESEL', 'AUTOMATIC', 5, 'L', 11200.00, 620.00, 250, 30.00, 168, 'https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 5.0),
(6, 'Mercedes-Benz', 'C200 Avantgarde', 'DL03-MB-2211', 'LUXURY', 'PETROL', 'AUTOMATIC', 5, 'L', 9800.00, 520.00, 250, 28.00, 155, 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 4.8),
(7, 'Honda', 'City ZX e:HEV Hybrid', 'DL09-HC-4400', 'SEDAN', 'PETROL', 'AUTOMATIC', 5, 'L', 2700.00, 150.00, 300, 12.00, 98, 'https://images.unsplash.com/photo-1617814076367-b759c7d7e738?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 4.7),
(8, 'Skoda', 'Slavia 1.5 TSI Style', 'MH12-SK-8812', 'SEDAN', 'PETROL', 'AUTOMATIC', 5, 'L', 2950.00, 165.00, 300, 14.00, 132, 'https://images.unsplash.com/photo-1580273916550-e323be2ae537?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 4.7),
(9, 'Toyota', 'Fortuner Legender 4x4', 'UP16-TF-9900', 'SUV', 'DIESEL', 'AUTOMATIC', 7, 'L', 5900.00, 320.00, 300, 18.00, 192, 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 4.9),
(10, 'Tata', 'Nexon EV Empowered+', 'DL08-EV-4411', 'EV', 'EV', 'AUTOMATIC', 5, 'M', 3300.00, 190.00, 300, 12.00, 0, 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 4.8),
(11, 'Hyundai', 'i20 N-Line DCT', 'DL07-IN-7007', 'HATCHBACK', 'PETROL', 'AUTOMATIC', 5, 'S', 2100.00, 120.00, 300, 10.00, 125, 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 4.6),
(12, 'Maruti Suzuki', 'Swift ZXi AMT', 'DL02-SW-3322', 'HATCHBACK', 'PETROL', 'AUTOMATIC', 5, 'S', 1650.00, 95.00, 300, 9.00, 110, 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 4.5),
(13, 'Maruti Suzuki', 'Ertiga ZXi CNG', 'DL05-ER-5511', 'SUV', 'CNG', 'MANUAL', 7, 'M', 2350.00, 130.00, 300, 10.00, 105, 'https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 4.5);

-- 4. Sample Completed Booking
INSERT INTO bookings (id, booking_code, user_id, car_id, pickup_location_id, start_time, end_time, actual_return_time, status, trip_type, base_amount, surcharge_amount, discount_amount, tax_amount, total_amount, deposit_amount, extra_charges, est_distance_km, est_co2_kg, created_at) VALUES
(1, 'DS-2026-000123', 2, 1, 1, '2026-10-10 09:00:00', '2026-10-12 09:00:00', '2026-10-12 08:45:00', 'COMPLETED', 'FAMILY_ROAD_TRIP', 5800.00, 580.00, 174.00, 1117.08, 7323.08, 1500.00, 0.00, 450, 60.75, '2026-10-01 10:00:00');

-- 5. Condition Reports for Completed Booking
INSERT INTO condition_reports (id, booking_id, stage, fuel_percent, odometer_km, damage_zones, notes, created_at) VALUES
(1, 1, 'PICKUP', 100, 14200, 'LEFT', 'Minor hairline scratch on left rear door.', '2026-10-10 09:00:00'),
(2, 1, 'RETURN', 100, 14640, 'LEFT', 'Vehicle returned in clean condition on time. Fuel verified full.', '2026-10-12 08:45:00');

-- 6. Verified Review
INSERT INTO reviews (id, booking_id, user_id, car_id, rating, comment, created_at) VALUES
(1, 1, 2, 1, 5, 'Seamless rental experience! The Creta was immaculate, fuel tank was full, and the digital condition report gave total peace of mind.', '2026-10-12 10:00:00');
