package com.drivesense.controller;

import com.drivesense.entity.Booking;
import com.drivesense.entity.Car;
import com.drivesense.entity.TrustEvent;
import com.drivesense.entity.User;
import com.drivesense.enums.BookingStatus;
import com.drivesense.enums.CarStatus;
import com.drivesense.repository.BookingRepository;
import com.drivesense.repository.CarRepository;
import com.drivesense.repository.TrustEventRepository;
import com.drivesense.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/renter")
@PreAuthorize("isAuthenticated()")
public class RenterController {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final TrustEventRepository trustEventRepository;

    public RenterController(UserRepository userRepository,
                            BookingRepository bookingRepository,
                            CarRepository carRepository,
                            TrustEventRepository trustEventRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
        this.trustEventRepository = trustEventRepository;
    }

    private User getAuthenticatedUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found: " + userDetails.getUsername()));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getAuthenticatedUser(userDetails);
        List<Booking> allBookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        Booking upcoming = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.ACTIVE)
                .findFirst()
                .orElse(null);

        long completedCount = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
        long upcomingCount = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();

        List<Car> recommendedCars = carRepository.findTop6ByStatusOrderByAvgRatingDesc(CarStatus.AVAILABLE);

        model.addAttribute("user", user);
        model.addAttribute("upcomingBooking", upcoming);
        model.addAttribute("allBookings", allBookings);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("upcomingCount", upcomingCount);
        model.addAttribute("savedCount", 3); // Seeded saved vehicles
        model.addAttribute("recommendedCars", recommendedCars);
        model.addAttribute("activeNav", "dashboard");
        return "renter/dashboard";
    }

    @GetMapping("/bookings")
    public String bookings(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(required = false) BookingStatus status,
                           Model model) {
        User user = getAuthenticatedUser(userDetails);
        List<Booking> bookings = (status != null)
                ? bookingRepository.findByUserIdAndStatus(user.getId(), status)
                : bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("currentStatus", status);
        model.addAttribute("activeNav", "bookings");
        return "renter/bookings";
    }

    @GetMapping("/saved")
    public String savedCars(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getAuthenticatedUser(userDetails);
        List<Car> savedCars = carRepository.findTop6ByStatusOrderByAvgRatingDesc(CarStatus.AVAILABLE);

        model.addAttribute("user", user);
        model.addAttribute("savedCars", savedCars);
        model.addAttribute("activeNav", "saved");
        return "renter/saved";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getAuthenticatedUser(userDetails);
        List<TrustEvent> trustEvents = trustEventRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("trustEvents", trustEvents);
        model.addAttribute("activeNav", "profile");
        return "renter/profile";
    }

    @GetMapping("/invoices")
    public String invoices(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getAuthenticatedUser(userDetails);
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("activeNav", "invoices");
        return "renter/invoices";
    }
}
