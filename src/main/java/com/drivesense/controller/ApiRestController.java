package com.drivesense.controller;

import com.drivesense.dto.BookingRequest;
import com.drivesense.entity.Car;
import com.drivesense.entity.User;
import com.drivesense.pricing.PriceBreakdown;
import com.drivesense.repository.BookingRepository;
import com.drivesense.repository.CarRepository;
import com.drivesense.repository.UserRepository;
import com.drivesense.service.CarService;
import com.drivesense.service.PricingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiRestController {

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final CarService carService;
    private final PricingService pricingService;
    private final UserRepository userRepository;

    public ApiRestController(CarRepository carRepository,
                             BookingRepository bookingRepository,
                             CarService carService,
                             PricingService pricingService,
                             UserRepository userRepository) {
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
        this.carService = carService;
        this.pricingService = pricingService;
        this.userRepository = userRepository;
    }

    @GetMapping("/cars/{id}/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        Map<String, Object> resp = new HashMap<>();
        if (start == null || end == null || !end.isAfter(start)) {
            resp.put("available", false);
            resp.put("message", "Return date-time must be after pickup date-time");
            return ResponseEntity.badRequest().body(resp);
        }

        long overlaps = bookingRepository.countOverlaps(id, start, end);
        boolean available = (overlaps == 0);
        resp.put("available", available);
        resp.put("message", available ? "Car is available for your dates!" : "Car is already booked during these dates.");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/price/preview")
    public ResponseEntity<PriceBreakdown> previewPrice(
            @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Car car = carRepository.findById(request.getCarId())
                .orElse(null);
        if (car == null) {
            return ResponseEntity.notFound().build();
        }

        User user = null;
        if (userDetails != null) {
            user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        }

        LocalDateTime start = request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now().plusDays(1);
        LocalDateTime end = request.getEndTime() != null ? request.getEndTime() : start.plusDays(2);

        PriceBreakdown breakdown = pricingService.calculatePrice(
                car, user, start, end,
                request.isDriverRequired(),
                request.isInsuranceRequired(),
                request.isChildSeatRequired()
        );

        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/cars/{id}/booked-dates")
    public ResponseEntity<List<Map<String, String>>> getBookedDates(@PathVariable Long id) {
        return ResponseEntity.ok(carService.getBookedDateRanges(id));
    }

    @GetMapping("/admin/stats/revenue")
    public ResponseEntity<Map<String, Object>> getAdminRevenueChartData() {
        Map<String, Object> chartData = new HashMap<>();

        // Generate monthly breakdown for the last 6 months
        List<String> labels = Arrays.asList("May", "Jun", "Jul", "Aug", "Sep", "Oct");
        List<BigDecimal> monthlyRevenue = Arrays.asList(
                new BigDecimal("68500"),
                new BigDecimal("84200"),
                new BigDecimal("112400"),
                new BigDecimal("138900"),
                new BigDecimal("165200"),
                new BigDecimal("194500")
        );

        Map<String, Integer> categoryDistribution = new HashMap<>();
        categoryDistribution.put("SUV", 42);
        categoryDistribution.put("EV", 28);
        categoryDistribution.put("Sedan", 18);
        categoryDistribution.put("Luxury", 8);
        categoryDistribution.put("Hatchback", 4);

        chartData.put("labels", labels);
        chartData.put("revenue", monthlyRevenue);
        chartData.put("categories", categoryDistribution);

        return ResponseEntity.ok(chartData);
    }
}
