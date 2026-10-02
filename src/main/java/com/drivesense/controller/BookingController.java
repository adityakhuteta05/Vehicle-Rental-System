package com.drivesense.controller;

import com.drivesense.dto.BookingRequest;
import com.drivesense.entity.Booking;
import com.drivesense.entity.Car;
import com.drivesense.entity.Location;
import com.drivesense.entity.User;
import com.drivesense.enums.TripType;
import com.drivesense.pricing.PriceBreakdown;
import com.drivesense.repository.LocationRepository;
import com.drivesense.repository.UserRepository;
import com.drivesense.service.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final CarService carService;
    private final LocationRepository locationRepository;
    private final PricingService pricingService;
    private final InvoiceXmlService invoiceXmlService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService,
                             CarService carService,
                             LocationRepository locationRepository,
                             PricingService pricingService,
                             InvoiceXmlService invoiceXmlService,
                             ReviewService reviewService,
                             UserRepository userRepository) {
        this.bookingService = bookingService;
        this.carService = carService;
        this.locationRepository = locationRepository;
        this.pricingService = pricingService;
        this.invoiceXmlService = invoiceXmlService;
        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }

    @GetMapping("/book/{carId}")
    public String showBookingPage(@PathVariable Long carId,
                                  @RequestParam(required = false) TripType tripType,
                                  @RequestParam(required = false) String start,
                                  @RequestParam(required = false) String end,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        Car car = carService.getCarById(carId);
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        List<Location> locations = locationRepository.findAll();

        BookingRequest request = new BookingRequest();
        request.setCarId(carId);
        request.setTripType(tripType);
        if (start != null) {
            try { request.setStartTime(LocalDateTime.parse(start)); } catch (Exception ignored) {}
        }
        if (end != null) {
            try { request.setEndTime(LocalDateTime.parse(end)); } catch (Exception ignored) {}
        }

        PriceBreakdown initialBreakdown = pricingService.calculatePrice(
                car, user, request.getStartTime(), request.getEndTime(),
                request.isDriverRequired(), request.isInsuranceRequired(), request.isChildSeatRequired()
        );

        model.addAttribute("car", car);
        model.addAttribute("user", user);
        model.addAttribute("locations", locations);
        model.addAttribute("bookingRequest", request);
        model.addAttribute("priceBreakdown", initialBreakdown);
        model.addAttribute("activeNav", "catalogue");

        return "booking";
    }

    @PostMapping("/book/{carId}")
    public String processBooking(@PathVariable Long carId,
                                 @ModelAttribute BookingRequest bookingRequest,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        bookingRequest.setCarId(carId);

        try {
            Booking booking = bookingService.createBooking(user, bookingRequest);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Booking confirmed! Your reference code is " + booking.getBookingCode() + ".");
            return "redirect:/my-bookings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/book/" + carId;
        }
    }

    @GetMapping("/my-bookings")
    public String myBookings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        return "redirect:/renter/bookings";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        try {
            Booking cancelled = bookingService.cancelBooking(id, user, isAdmin);
            if (cancelled.getExtraCharges() != null && cancelled.getExtraCharges().signum() > 0) {
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Booking cancelled within 24h of pickup. A 20% late cancellation fee of ₹" + cancelled.getExtraCharges() + " applies.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Booking cancelled successfully. Full refund initiated.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/my-bookings";
    }

    @GetMapping("/bookings/{id}/invoice")
    public String viewPrintableInvoice(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       Model model) {
        Booking booking = bookingService.getBookingById(id);
        User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !booking.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Unauthorized to access this invoice.");
        }

        model.addAttribute("booking", booking);
        model.addAttribute("user", booking.getUser());
        model.addAttribute("car", booking.getCar());

        return "invoice";
    }

    @GetMapping(value = "/bookings/{id}/invoice.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> downloadXmlInvoice(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        Booking booking = bookingService.getBookingById(id);
        User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !booking.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        String xmlContent = invoiceXmlService.generateAndValidateXmlInvoice(booking);
        byte[] bytes = xmlContent.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + booking.getBookingCode() + ".xml\"")
                .contentType(MediaType.APPLICATION_XML)
                .body(bytes);
    }

    @PostMapping("/bookings/{id}/review")
    public String submitReview(@PathVariable Long id,
                               @RequestParam int rating,
                               @RequestParam String comment,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        try {
            reviewService.addReview(id, user, rating, comment);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thank you for your feedback! Your verified review has been published.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-bookings";
    }
}
