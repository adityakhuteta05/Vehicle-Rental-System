package com.drivesense.config;

import com.drivesense.entity.*;
import com.drivesense.enums.*;
import com.drivesense.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final LocationRepository locationRepository;
    private final BookingRepository bookingRepository;
    private final ConditionReportRepository conditionReportRepository;
    private final ReviewRepository reviewRepository;
    private final TrustEventRepository trustEventRepository;
    private final PaymentRepository paymentRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CarRepository carRepository,
                           LocationRepository locationRepository,
                           BookingRepository bookingRepository,
                           ConditionReportRepository conditionReportRepository,
                           ReviewRepository reviewRepository,
                           TrustEventRepository trustEventRepository,
                           PaymentRepository paymentRepository,
                           OwnerProfileRepository ownerProfileRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.locationRepository = locationRepository;
        this.bookingRepository = bookingRepository;
        this.conditionReportRepository = conditionReportRepository;
        this.reviewRepository = reviewRepository;
        this.trustEventRepository = trustEventRepository;
        this.paymentRepository = paymentRepository;
        this.ownerProfileRepository = ownerProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded with demo data.");
            return;
        }

        log.info("Starting DriveSense initial data population...");

        // 1. Seed Locations
        Location loc1 = locationRepository.save(new Location("Airport T3 Mobility Hub", "New Delhi", "Terminal 3 Arrival Lane 4, IGI Airport"));
        Location loc2 = locationRepository.save(new Location("Connaught Place Flagship", "New Delhi", "Inner Circle, Block C, CP"));
        locationRepository.save(new Location("Cyber City Hub", "Gurugram", "Building 10 DLF Cyber City"));
        locationRepository.save(new Location("Koramangala Station", "Bengaluru", "80 Feet Road, 4th Block"));
        locationRepository.save(new Location("BKC Executive Lounge", "Mumbai", "G Block, Bandra Kurla Complex"));

        // 2. Seed Users & Marketplace Actors
        // Admin
        User admin = new User(
                "Karan Mehta (Platform Admin)",
                "admin@drivesense.com",
                "+91 98765 00001",
                passwordEncoder.encode("admin123"),
                "DL-ADMIN-9999",
                LocalDate.of(1990, 5, 12),
                "ROLE_ADMIN"
        );
        admin.setTrustScore(100);
        admin.setCity("New Delhi");
        userRepository.save(admin);

        // Vehicle Owner
        User owner = new User(
                "Rajesh Singhania",
                "owner@drivesense.com",
                "+91 98100 55443",
                passwordEncoder.encode("owner123"),
                "DL-042008009988",
                LocalDate.of(1986, 11, 4),
                "ROLE_OWNER"
        );
        owner.setTrustScore(100);
        owner.setCity("Gurugram");
        owner.setAddress("Tower 4, The Magnolias, Golf Course Road");
        owner.setGovernmentId("DL-CORP-4492");
        userRepository.save(owner);

        // Owner Profile
        OwnerProfile ownerProfile = new OwnerProfile(
                owner,
                "Apex Mobility & Fleet Partners",
                "DL-CORP-4492",
                "Building 10, DLF Cyber City",
                "Gurugram"
        );
        ownerProfile.setTotalEarnings(new BigDecimal("184500.00"));
        ownerProfile.setOwnerRating(new BigDecimal("4.9"));
        ownerProfileRepository.save(ownerProfile);

        // Renters
        User customer = new User(
                "Aaditya Sharma",
                "user@example.com",
                "+91 98765 43210",
                passwordEncoder.encode("user123"),
                "DL-042015001234",
                LocalDate.of(1998, 8, 20),
                "ROLE_RENTER"
        );
        customer.setTrustScore(78); // Gold tier
        customer.setCity("New Delhi");
        userRepository.save(customer);

        User vipCustomer = new User(
                "Rohan Verma",
                "vip@example.com",
                "+91 98111 22334",
                passwordEncoder.encode("vip123"),
                "DL-012012009876",
                LocalDate.of(1992, 3, 15),
                "ROLE_RENTER"
        );
        vipCustomer.setTrustScore(92); // Platinum tier
        vipCustomer.setCity("Mumbai");
        userRepository.save(vipCustomer);

        // Seed Trust events for Aaditya
        trustEventRepository.save(new TrustEvent(customer, "Initial joining trust calibration", 50, 50));
        trustEventRepository.save(new TrustEvent(customer, "Completed Goa road trip on time with clean car (+5)", 5, 55));
        trustEventRepository.save(new TrustEvent(customer, "Verified 5-star trip review (+1)", 1, 56));
        trustEventRepository.save(new TrustEvent(customer, "Returned weekend rental with zero damage (+5)", 5, 61));
        trustEventRepository.save(new TrustEvent(customer, "Unlocked Gold Loyalty Tier benefit status", 17, 78));

        // 3. Seed Cars (13 premium, diversified vehicles)
        Car car1 = createCar(owner, "Hyundai", "Creta SX(O) Turbo", "DL04-CC-1024", CarType.SUV, FuelType.PETROL,
                Transmission.AUTOMATIC, 5, BootSize.M, new BigDecimal("2900.00"), new BigDecimal("165.00"),
                300, new BigDecimal("12.00"), 135,
                "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=900&q=80",
                new BigDecimal("4.8"),
                Arrays.asList("Panoramic Sunroof", "Ventilated Seats", "Bose Premium Audio", "ADAS Level 2", "Wireless Apple CarPlay"));

        Car car2 = createCar(owner, "Mahindra", "XUV700 AX7 Luxury", "HR26-DX-7700", CarType.SUV, FuelType.DIESEL,
                Transmission.AUTOMATIC, 7, BootSize.L, new BigDecimal("4300.00"), new BigDecimal("240.00"),
                300, new BigDecimal("15.00"), 165,
                "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=900&q=80",
                new BigDecimal("4.9"),
                Arrays.asList("7 Seater", "Sony 3D Sound", "All-Wheel Drive", "Panoramic Skyroof", "Blind View Monitor"));

        Car car3 = createCar(owner, "Tesla", "Model 3 Long Range", "DL01-EV-3001", CarType.EV, FuelType.EV,
                Transmission.AUTOMATIC, 5, BootSize.M, new BigDecimal("5900.00"), new BigDecimal("320.00"),
                350, new BigDecimal("18.00"), 0,
                "https://images.unsplash.com/photo-1560958089-b8a1929cea89?auto=format&fit=crop&w=900&q=80",
                new BigDecimal("4.9"),
                Arrays.asList("Zero Tailpipe Emissions", "Autopilot", "15-inch Touchscreen", "Glass Roof", "Supercharger Network"));

        createCar(owner, "Hyundai", "Ioniq 5 Lounge", "KA05-EV-9999", CarType.EV, FuelType.EV,
                Transmission.AUTOMATIC, 5, BootSize.L, new BigDecimal("6800.00"), new BigDecimal("360.00"),
                350, new BigDecimal("20.00"), 0,
                "https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=900&q=80",
                new BigDecimal("4.9"),
                Arrays.asList("Ultra-Fast 800V Charging", "Relaxation Comfort Seats", "V2L Power Output", "Augmented Reality HUD"));

        createCar(owner, "BMW", "530d M-Sport Executive", "MH01-BM-5050", CarType.LUXURY, FuelType.DIESEL,
                Transmission.AUTOMATIC, 5, BootSize.L, new BigDecimal("11200.00"), new BigDecimal("620.00"),
                250, new BigDecimal("30.00"), 168,
                "https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=900&q=80",
                new BigDecimal("5.0"),
                Arrays.asList("Executive Chauffeur Grade", "Harman Kardon Surround", "Adaptive M Suspension", "Gesture Control"));

        createCar(owner, "Mercedes-Benz", "C200 Avantgarde", "DL03-MB-2211", CarType.LUXURY, FuelType.PETROL,
                Transmission.AUTOMATIC, 5, BootSize.L, new BigDecimal("9800.00"), new BigDecimal("520.00"),
                250, new BigDecimal("28.00"), 155,
                "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=900&q=80",
                new BigDecimal("4.8"),
                Arrays.asList("Burmester 3D Sound", "Ambient Lighting 64 Colors", "Executive Leather", "Sunroof"));

        createCar(admin, "Honda", "City ZX e:HEV Hybrid", "DL09-HC-4400", CarType.SEDAN, FuelType.PETROL,
                Transmission.AUTOMATIC, 5, BootSize.L, new BigDecimal("2700.00"), new BigDecimal("150.00"),
                300, new BigDecimal("12.00"), 98,
                "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?auto=format&fit=crop&w=900&q=80",
                new BigDecimal("4.7"),
                Arrays.asList("Strong Hybrid 27 km/l", "Honda Sensing ADAS", "LaneWatch Camera", "Electric Sunroof"));

        createCar(admin, "Skoda", "Slavia 1.5 TSI Style", "MH12-SK-8812", CarType.SEDAN, FuelType.PETROL,
                Transmission.AUTOMATIC, 5, BootSize.L, new BigDecimal("2950.00"), new BigDecimal("165.00"),
                300, new BigDecimal("14.00"), 132,
                "https://images.unsplash.com/photo-1580273916550-e323be2ae537?auto=format&fit=crop&w=900&q=80",
                new BigDecimal("4.7"),
                Arrays.asList("521L Massive Boot", "5-Star Global NCAP Safety", "Ventilated Seats", "DSG Paddle Shifters"));

        createCar(admin, "Toyota", "Fortuner Legender 4x4", "UP16-TF-9900", CarType.SUV, FuelType.DIESEL,
                Transmission.AUTOMATIC, 7, BootSize.L, new BigDecimal("5900.00"), new BigDecimal("320.00"),
                300, new BigDecimal("18.00"), 192,
                "https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?auto=format&fit=crop&w=900&q=80",
                new BigDecimal("4.9"),
                Arrays.asList("4x4 High/Low Range", "Hill Descent Control", "7 Seats", "JBL 11 Speaker System"));

        createCar(owner, "Tata", "Nexon EV Empowered+", "DL08-EV-4411", CarType.EV, FuelType.EV,
                Transmission.AUTOMATIC, 5, BootSize.M, new BigDecimal("3300.00"), new BigDecimal("190.00"),
                300, new BigDecimal("12.00"), 0,
                "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&w=900&q=80",
                new BigDecimal("4.8"),
                Arrays.asList("465 km Long Range", "360-Degree Camera", "Fast DC Charging", "Ventilated Front Seats"));

        createCar(admin, "Hyundai", "i20 N-Line DCT", "DL07-IN-7007", CarType.HATCHBACK, FuelType.PETROL,
                Transmission.AUTOMATIC, 5, BootSize.S, new BigDecimal("2100.00"), new BigDecimal("120.00"),
                300, new BigDecimal("10.00"), 125,
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=900&q=80",
                new BigDecimal("4.6"),
                Arrays.asList("Sport Tuned Exhaust", "Paddle Shifters", "Bose Audio", "All 4 Disc Brakes"));

        createCar(admin, "Maruti Suzuki", "Swift ZXi AMT", "DL02-SW-3322", CarType.HATCHBACK, FuelType.PETROL,
                Transmission.AUTOMATIC, 5, BootSize.S, new BigDecimal("1650.00"), new BigDecimal("95.00"),
                300, new BigDecimal("9.00"), 110,
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=900&q=80",
                new BigDecimal("4.5"),
                Arrays.asList("25 km/l High Mileage", "Keyless Smart Entry", "Apple CarPlay", "Compact City Agility"));

        createCar(owner, "Maruti Suzuki", "Ertiga ZXi CNG", "DL05-ER-5511", CarType.SUV, FuelType.CNG,
                Transmission.MANUAL, 7, BootSize.M, new BigDecimal("2350.00"), new BigDecimal("130.00"),
                300, new BigDecimal("10.00"), 105,
                "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=900&q=80",
                new BigDecimal("4.5"),
                Arrays.asList("7 Seater Family", "Eco CNG Economy", "Rear AC Vents", "Flexible Foldable Rows"));

        // 4. Seed demo completed booking with condition reports and review
        Booking pastBooking = new Booking();
        pastBooking.setBookingCode("DS-2026-000123");
        pastBooking.setUser(customer);
        pastBooking.setCar(car1);
        pastBooking.setPickupLocation(loc1);
        pastBooking.setStartTime(LocalDateTime.now().minusDays(5).withHour(9).withMinute(0));
        pastBooking.setEndTime(LocalDateTime.now().minusDays(2).withHour(18).withMinute(0));
        pastBooking.setActualReturnTime(LocalDateTime.now().minusDays(2).withHour(17).withMinute(45));
        pastBooking.setStatus(BookingStatus.COMPLETED);
        pastBooking.setTripType(TripType.FAMILY_ROAD_TRIP);
        pastBooking.setBaseAmount(new BigDecimal("8700.00"));
        pastBooking.setSurchargeAmount(new BigDecimal("870.00"));
        pastBooking.setDiscountAmount(new BigDecimal("435.00"));
        pastBooking.setTaxAmount(new BigDecimal("1644.30"));
        pastBooking.setTotalAmount(new BigDecimal("10779.30"));
        pastBooking.setPlatformFee(new BigDecimal("1077.93"));
        pastBooking.setOwnerEarnings(new BigDecimal("9701.37"));
        pastBooking.setDepositAmount(new BigDecimal("1500.00")); // Gold tier 50% waiver
        pastBooking.setExtraCharges(BigDecimal.ZERO);
        pastBooking.setEstDistanceKm(450);
        pastBooking.setEstCo2Kg(new BigDecimal("60.75"));
        pastBooking.setCreatedAt(LocalDateTime.now().minusDays(10));
        pastBooking = bookingRepository.save(pastBooking);

        paymentRepository.save(new Payment(pastBooking, "CARD", new BigDecimal("12279.30"), "SUCCESS", "TXN-20261010-8821"));

        // Seed Pickup & Return Condition Reports for past booking
        ConditionReport pickupReport = new ConditionReport(pastBooking, ReportStage.PICKUP, 100, 14200, "LEFT", "Minor hairline scratch on left rear door.");
        pickupReport.setCreatedAt(pastBooking.getStartTime());
        conditionReportRepository.save(pickupReport);

        ConditionReport returnReport = new ConditionReport(pastBooking, ReportStage.RETURN, 100, 14640, "LEFT", "Vehicle returned in clean condition on time. Fuel verified.");
        returnReport.setCreatedAt(pastBooking.getActualReturnTime());
        conditionReportRepository.save(returnReport);

        // Seed verified customer review
        Review review = new Review(pastBooking, customer, car1, 5,
                "Seamless rental experience! The Creta was immaculate, fuel tank was full, and the digital condition report gave total peace of mind. Highly recommend DriveSense!");
        reviewRepository.save(review);

        // 5. Seed an active booking (Car out now)
        Booking activeBooking = new Booking();
        activeBooking.setBookingCode("DS-2026-000456");
        activeBooking.setUser(customer);
        activeBooking.setCar(car2);
        activeBooking.setPickupLocation(loc2);
        activeBooking.setStartTime(LocalDateTime.now().minusHours(8));
        activeBooking.setEndTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0));
        activeBooking.setStatus(BookingStatus.ACTIVE);
        activeBooking.setTripType(TripType.HILL_DRIVE);
        activeBooking.setBaseAmount(new BigDecimal("8600.00"));
        activeBooking.setSurchargeAmount(new BigDecimal("700.00"));
        activeBooking.setDiscountAmount(new BigDecimal("258.00"));
        activeBooking.setTaxAmount(new BigDecimal("1627.56"));
        activeBooking.setTotalAmount(new BigDecimal("10669.56"));
        activeBooking.setPlatformFee(new BigDecimal("1066.96"));
        activeBooking.setOwnerEarnings(new BigDecimal("9602.60"));
        activeBooking.setDepositAmount(new BigDecimal("1500.00"));
        activeBooking.setExtraCharges(BigDecimal.ZERO);
        activeBooking.setEstDistanceKm(500);
        activeBooking.setEstCo2Kg(new BigDecimal("82.50"));
        activeBooking.setCreatedAt(LocalDateTime.now().minusDays(2));
        activeBooking = bookingRepository.save(activeBooking);

        paymentRepository.save(new Payment(activeBooking, "UPI", new BigDecimal("12169.56"), "SUCCESS", "TXN-UPI-99482710"));

        ConditionReport activePickup = new ConditionReport(activeBooking, ReportStage.PICKUP, 100, 8500, "FRONT", "Small chip on front lower bumper recorded at departure.");
        activePickup.setCreatedAt(activeBooking.getStartTime());
        conditionReportRepository.save(activePickup);

        // 6. Seed a Pending Booking Request waiting for Owner approval
        Booking pendingBooking = new Booking();
        pendingBooking.setBookingCode("DS-2026-000789");
        pendingBooking.setUser(vipCustomer);
        pendingBooking.setCar(car3);
        pendingBooking.setPickupLocation(loc1);
        pendingBooking.setStartTime(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0));
        pendingBooking.setEndTime(LocalDateTime.now().plusDays(4).withHour(18).withMinute(0));
        pendingBooking.setStatus(BookingStatus.PENDING);
        pendingBooking.setTripType(TripType.WEDDING);
        pendingBooking.setBaseAmount(new BigDecimal("11800.00"));
        pendingBooking.setSurchargeAmount(new BigDecimal("1180.00"));
        pendingBooking.setDiscountAmount(new BigDecimal("708.00"));
        pendingBooking.setTaxAmount(new BigDecimal("2208.96"));
        pendingBooking.setTotalAmount(new BigDecimal("14480.96"));
        pendingBooking.setPlatformFee(new BigDecimal("1448.10"));
        pendingBooking.setOwnerEarnings(new BigDecimal("13032.86"));
        pendingBooking.setDepositAmount(BigDecimal.ZERO); // Platinum tier 100% waiver
        pendingBooking.setEstDistanceKm(350);
        pendingBooking.setEstCo2Kg(BigDecimal.ZERO);
        pendingBooking.setCreatedAt(LocalDateTime.now().minusHours(2));
        bookingRepository.save(pendingBooking);

        log.info("DriveSense database successfully seeded with cars, users, locations, and live demo bookings!");
    }

    private Car createCar(User owner, String brand, String model, String regNumber, CarType type, FuelType fuelType,
                          Transmission transmission, int seats, BootSize bootSize, BigDecimal pricePerDay,
                          BigDecimal pricePerHour, int kmPerDayLimit, BigDecimal extraKmRate, int co2GPerKm,
                          String imageUrl, BigDecimal avgRating, List<String> features) {
        Car car = new Car(brand, model, regNumber, type, fuelType, transmission, seats, bootSize,
                pricePerDay, pricePerHour, kmPerDayLimit, extraKmRate, co2GPerKm, imageUrl, CarStatus.AVAILABLE, avgRating);
        car.setOwner(owner);
        car.setFeatures(features);
        return carRepository.save(car);
    }
}
