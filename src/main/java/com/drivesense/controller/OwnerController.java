package com.drivesense.controller;

import com.drivesense.entity.*;
import com.drivesense.enums.*;
import com.drivesense.repository.UserRepository;
import com.drivesense.service.OwnerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/owner")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class OwnerController {

    private final OwnerService ownerService;
    private final UserRepository userRepository;

    public OwnerController(OwnerService ownerService,
                           UserRepository userRepository) {
        this.ownerService = ownerService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedOwner(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Owner not found: " + userDetails.getUsername()));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User owner = getAuthenticatedOwner(userDetails);
        Map<String, Object> stats = ownerService.getDashboardStats(owner);
        OwnerProfile profile = ownerService.getOrCreateProfile(owner);

        model.addAttribute("owner", owner);
        model.addAttribute("profile", profile);
        model.addAttribute("stats", stats);
        model.addAttribute("activeNav", "dashboard");
        return "owner/dashboard";
    }

    @GetMapping("/vehicles")
    public String vehicles(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User owner = getAuthenticatedOwner(userDetails);
        List<Car> vehicles = ownerService.getOwnerVehicles(owner);

        model.addAttribute("owner", owner);
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("activeNav", "vehicles");
        return "owner/vehicles";
    }

    @GetMapping("/vehicles/add")
    public String addVehicleForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User owner = getAuthenticatedOwner(userDetails);

        model.addAttribute("owner", owner);
        model.addAttribute("car", new Car());
        model.addAttribute("carTypes", CarType.values());
        model.addAttribute("fuelTypes", FuelType.values());
        model.addAttribute("transmissions", Transmission.values());
        model.addAttribute("bootSizes", BootSize.values());
        model.addAttribute("activeNav", "vehicles");
        return "owner/vehicle-add";
    }

    @PostMapping("/vehicles/add")
    public String saveNewVehicle(@AuthenticationPrincipal UserDetails userDetails,
                                 @ModelAttribute Car car,
                                 @RequestParam(required = false) List<String> featureList,
                                 RedirectAttributes redirectAttributes) {
        User owner = getAuthenticatedOwner(userDetails);
        if (featureList != null) {
            car.setFeatures(featureList);
        }
        ownerService.saveVehicle(car, owner);
        redirectAttributes.addFlashAttribute("successMessage", "Vehicle " + car.getDisplayName() + " published successfully to the marketplace!");
        return "redirect:/owner/vehicles";
    }

    @GetMapping("/vehicles/{id}/edit")
    public String editVehicleForm(@AuthenticationPrincipal UserDetails userDetails,
                                  @PathVariable Long id,
                                  Model model) {
        User owner = getAuthenticatedOwner(userDetails);
        Car car = ownerService.getVehicleForOwner(id, owner);

        model.addAttribute("owner", owner);
        model.addAttribute("car", car);
        model.addAttribute("carTypes", CarType.values());
        model.addAttribute("fuelTypes", FuelType.values());
        model.addAttribute("transmissions", Transmission.values());
        model.addAttribute("bootSizes", BootSize.values());
        model.addAttribute("carStatuses", CarStatus.values());
        model.addAttribute("activeNav", "vehicles");
        return "owner/vehicle-edit";
    }

    @PostMapping("/vehicles/{id}/edit")
    public String updateVehicle(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long id,
                                @ModelAttribute Car formCar,
                                RedirectAttributes redirectAttributes) {
        User owner = getAuthenticatedOwner(userDetails);
        Car existing = ownerService.getVehicleForOwner(id, owner);

        existing.setBrand(formCar.getBrand());
        existing.setModel(formCar.getModel());
        existing.setType(formCar.getType());
        existing.setFuelType(formCar.getFuelType());
        existing.setTransmission(formCar.getTransmission());
        existing.setSeats(formCar.getSeats());
        existing.setBootSize(formCar.getBootSize());
        existing.setPricePerDay(formCar.getPricePerDay());
        existing.setPricePerHour(formCar.getPricePerHour());
        existing.setKmPerDayLimit(formCar.getKmPerDayLimit());
        existing.setExtraKmRate(formCar.getExtraKmRate());
        existing.setStatus(formCar.getStatus());
        if (formCar.getImageUrl() != null && !formCar.getImageUrl().isBlank()) {
            existing.setImageUrl(formCar.getImageUrl());
        }

        ownerService.saveVehicle(existing, owner);
        redirectAttributes.addFlashAttribute("successMessage", "Vehicle updated successfully.");
        return "redirect:/owner/vehicles";
    }

    @PostMapping("/vehicles/{id}/status")
    public String toggleStatus(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long id,
                               @RequestParam CarStatus status,
                               RedirectAttributes redirectAttributes) {
        User owner = getAuthenticatedOwner(userDetails);
        Car car = ownerService.getVehicleForOwner(id, owner);
        car.setStatus(status);
        ownerService.saveVehicle(car, owner);
        redirectAttributes.addFlashAttribute("successMessage", "Vehicle status set to " + status.name());
        return "redirect:/owner/vehicles";
    }

    @GetMapping("/bookings")
    public String bookings(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(required = false) BookingStatus status,
                           Model model) {
        User owner = getAuthenticatedOwner(userDetails);
        List<Booking> bookings = ownerService.getOwnerBookings(owner, status);

        model.addAttribute("owner", owner);
        model.addAttribute("bookings", bookings);
        model.addAttribute("currentStatus", status);
        model.addAttribute("activeNav", "bookings");
        return "owner/bookings";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetails(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long id,
                                 Model model) {
        User owner = getAuthenticatedOwner(userDetails);
        Booking booking = ownerService.getBookingForOwner(id, owner);

        model.addAttribute("owner", owner);
        model.addAttribute("booking", booking);
        model.addAttribute("activeNav", "bookings");
        return "owner/booking-details";
    }

    @PostMapping("/bookings/{id}/approve")
    public String approveBooking(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        User owner = getAuthenticatedOwner(userDetails);
        ownerService.updateBookingStatus(id, owner, BookingStatus.CONFIRMED);
        redirectAttributes.addFlashAttribute("successMessage", "Booking approved successfully!");
        return "redirect:/owner/bookings";
    }

    @PostMapping("/bookings/{id}/reject")
    public String rejectBooking(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        User owner = getAuthenticatedOwner(userDetails);
        ownerService.updateBookingStatus(id, owner, BookingStatus.CANCELLED);
        redirectAttributes.addFlashAttribute("infoMessage", "Booking request rejected.");
        return "redirect:/owner/bookings";
    }

    @PostMapping("/bookings/{id}/start")
    public String startRental(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        User owner = getAuthenticatedOwner(userDetails);
        ownerService.updateBookingStatus(id, owner, BookingStatus.ACTIVE);
        redirectAttributes.addFlashAttribute("successMessage", "Trip started! Vehicle is now out on rental.");
        return "redirect:/owner/bookings/" + id;
    }

    @PostMapping("/bookings/{id}/complete")
    public String completeRental(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        User owner = getAuthenticatedOwner(userDetails);
        ownerService.updateBookingStatus(id, owner, BookingStatus.COMPLETED);
        redirectAttributes.addFlashAttribute("successMessage", "Rental completed successfully! Net earnings credited to your wallet.");
        return "redirect:/owner/bookings/" + id;
    }

    @GetMapping("/calendar")
    public String calendar(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User owner = getAuthenticatedOwner(userDetails);
        List<Car> vehicles = ownerService.getOwnerVehicles(owner);
        List<Booking> bookings = ownerService.getOwnerBookings(owner, null);

        model.addAttribute("owner", owner);
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("bookings", bookings);
        model.addAttribute("activeNav", "calendar");
        return "owner/calendar";
    }

    @GetMapping("/earnings")
    public String earnings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User owner = getAuthenticatedOwner(userDetails);
        Map<String, Object> stats = ownerService.getDashboardStats(owner);
        List<Booking> bookings = ownerService.getOwnerBookings(owner, null);
        OwnerProfile profile = ownerService.getOrCreateProfile(owner);

        model.addAttribute("owner", owner);
        model.addAttribute("profile", profile);
        model.addAttribute("stats", stats);
        model.addAttribute("bookings", bookings);
        model.addAttribute("activeNav", "earnings");
        return "owner/earnings";
    }

    @GetMapping("/condition-reports")
    public String conditionReports(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User owner = getAuthenticatedOwner(userDetails);
        List<Booking> bookings = ownerService.getOwnerBookings(owner, null);

        model.addAttribute("owner", owner);
        model.addAttribute("bookings", bookings);
        model.addAttribute("activeNav", "dcr");
        return "owner/condition-reports";
    }

    @GetMapping("/reviews")
    public String reviews(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User owner = getAuthenticatedOwner(userDetails);
        List<Car> vehicles = ownerService.getOwnerVehicles(owner);

        model.addAttribute("owner", owner);
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("activeNav", "reviews");
        return "owner/reviews";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User owner = getAuthenticatedOwner(userDetails);
        OwnerProfile profile = ownerService.getOrCreateProfile(owner);

        model.addAttribute("owner", owner);
        model.addAttribute("profile", profile);
        model.addAttribute("activeNav", "profile");
        return "owner/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam String businessName,
                                @RequestParam String address,
                                @RequestParam String city,
                                RedirectAttributes redirectAttributes) {
        User owner = getAuthenticatedOwner(userDetails);
        owner.setFullName(fullName);
        owner.setPhone(phone);
        owner.setAddress(address);
        owner.setCity(city);
        userRepository.save(owner);

        OwnerProfile profile = ownerService.getOrCreateProfile(owner);
        profile.setBusinessName(businessName);
        profile.setAddress(address);
        profile.setCity(city);
        ownerService.getOrCreateProfile(owner);

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        return "redirect:/owner/profile";
    }
}
