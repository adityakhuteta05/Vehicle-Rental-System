package com.drivesense.controller;

import com.drivesense.dto.DcrComparisonDto;
import com.drivesense.entity.Booking;
import com.drivesense.entity.Car;
import com.drivesense.entity.ConditionReport;
import com.drivesense.entity.User;
import com.drivesense.enums.BookingStatus;
import com.drivesense.enums.CarStatus;
import com.drivesense.enums.CarType;
import com.drivesense.enums.DamageZone;
import com.drivesense.enums.FuelType;
import com.drivesense.enums.ReportStage;
import com.drivesense.enums.Transmission;
import com.drivesense.enums.BootSize;
import com.drivesense.repository.BookingRepository;
import com.drivesense.repository.CarRepository;
import com.drivesense.repository.UserRepository;
import com.drivesense.service.DcrService;
import com.drivesense.service.FleetImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final DcrService dcrService;
    private final FleetImportService fleetImportService;

    public AdminController(CarRepository carRepository,
                           BookingRepository bookingRepository,
                           UserRepository userRepository,
                           DcrService dcrService,
                           FleetImportService fleetImportService) {
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.dcrService = dcrService;
        this.fleetImportService = fleetImportService;
    }

    @GetMapping
    public String dashboard(Model model) {
        long totalCars = carRepository.count();
        long carsOutNow = bookingRepository.countCarsOutNow();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        long dueToday = bookingRepository.countDueToday(todayStart, todayEnd);
        long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
        BigDecimal monthlyRevenue = bookingRepository.sumTotalRevenueSince(LocalDateTime.now().minusDays(30));

        List<Booking> dueTodayList = bookingRepository.findDueTodayBookings(todayStart, todayEnd);
        List<Booking> outNowList = bookingRepository.findTop10ByStatusOrderByStartTimeAsc(BookingStatus.ACTIVE);
        List<Booking> recentBookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        if (recentBookings.size() > 8) {
            recentBookings = recentBookings.subList(0, 8);
        }

        model.addAttribute("totalCars", totalCars);
        model.addAttribute("carsOutNow", carsOutNow);
        model.addAttribute("dueToday", dueToday);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);
        model.addAttribute("dueTodayList", dueTodayList);
        model.addAttribute("outNowList", outNowList);
        model.addAttribute("recentBookings", recentBookings);
        model.addAttribute("adminTab", "dashboard");

        return "admin/dashboard";
    }

    @GetMapping("/cars")
    public String listCars(Model model) {
        List<Car> cars = carRepository.findAll();
        model.addAttribute("cars", cars);
        model.addAttribute("carTypes", CarType.values());
        model.addAttribute("fuelTypes", FuelType.values());
        model.addAttribute("transmissions", Transmission.values());
        model.addAttribute("bootSizes", BootSize.values());
        model.addAttribute("carStatuses", CarStatus.values());
        model.addAttribute("adminTab", "cars");
        return "admin/cars";
    }

    @PostMapping("/cars/add")
    public String addCar(@ModelAttribute Car car,
                         @RequestParam(required = false) String featuresInput,
                         RedirectAttributes redirectAttributes) {
        try {
            if (featuresInput != null && !featuresInput.trim().isEmpty()) {
                String[] feats = featuresInput.split(",");
                List<String> list = new ArrayList<>();
                for (String f : feats) {
                    if (!f.trim().isEmpty()) list.add(f.trim());
                }
                car.setFeatures(list);
            }
            carRepository.save(car);
            redirectAttributes.addFlashAttribute("successMessage", "Car " + car.getDisplayName() + " added to fleet.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding car: " + e.getMessage());
        }
        return "redirect:/admin/cars";
    }

    @PostMapping("/cars/{id}/status")
    public String updateCarStatus(@PathVariable Long id, @RequestParam CarStatus status, RedirectAttributes redirectAttributes) {
        Car car = carRepository.findById(id).orElseThrow();
        car.setStatus(status);
        carRepository.save(car);
        redirectAttributes.addFlashAttribute("successMessage", "Car " + car.getDisplayName() + " status updated to " + status.getDisplayName());
        return "redirect:/admin/cars";
    }

    @GetMapping("/bookings")
    public String listBookings(@RequestParam(required = false) BookingStatus status, Model model) {
        List<Booking> bookings;
        if (status != null) {
            bookings = bookingRepository.findTop10ByStatusOrderByStartTimeAsc(status);
        } else {
            bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        }
        model.addAttribute("bookings", bookings);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("adminTab", "bookings");
        return "admin/bookings";
    }

    @PostMapping("/bookings/{id}/approve")
    public String approveBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        redirectAttributes.addFlashAttribute("successMessage", "Booking " + booking.getBookingCode() + " approved.");
        return "redirect:/admin/bookings";
    }

    @GetMapping("/bookings/{id}/dcr/pickup")
    public String pickupDcrForm(@PathVariable Long id, Model model) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        ConditionReport existing = dcrService.getReport(id, ReportStage.PICKUP).orElse(null);

        model.addAttribute("booking", booking);
        model.addAttribute("report", existing);
        model.addAttribute("damageZones", DamageZone.values());
        model.addAttribute("adminTab", "bookings");
        return "admin/dcr-pickup";
    }

    @PostMapping("/bookings/{id}/dcr/pickup")
    public String submitPickupDcr(@PathVariable Long id,
                                  @RequestParam int fuelPercent,
                                  @RequestParam int odometerKm,
                                  @RequestParam(required = false) List<String> damageZones,
                                  @RequestParam(required = false) String notes,
                                  RedirectAttributes redirectAttributes) {
        try {
            dcrService.submitPickupReport(id, fuelPercent, odometerKm, damageZones, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Pickup Condition Report recorded! Trip is now marked ACTIVE.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/bookings/{id}/dcr/return")
    public String returnDcrForm(@PathVariable Long id, Model model) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        ConditionReport pickupReport = dcrService.getReport(id, ReportStage.PICKUP).orElse(null);
        ConditionReport returnReport = dcrService.getReport(id, ReportStage.RETURN).orElse(null);

        DcrComparisonDto comparison = null;
        if (pickupReport != null && returnReport != null) {
            comparison = dcrService.compareReports(id);
        }

        model.addAttribute("booking", booking);
        model.addAttribute("pickupReport", pickupReport);
        model.addAttribute("returnReport", returnReport);
        model.addAttribute("comparison", comparison);
        model.addAttribute("damageZones", DamageZone.values());
        model.addAttribute("adminTab", "bookings");
        return "admin/dcr-return";
    }

    @PostMapping("/bookings/{id}/dcr/return")
    public String submitReturnDcr(@PathVariable Long id,
                                  @RequestParam int fuelPercent,
                                  @RequestParam int odometerKm,
                                  @RequestParam(required = false) List<String> damageZones,
                                  @RequestParam(required = false) String notes,
                                  RedirectAttributes redirectAttributes) {
        try {
            DcrComparisonDto comparison = dcrService.submitReturnReport(id, fuelPercent, odometerKm, damageZones, notes);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Return inspection completed! Trip COMPLETED. Extra charges added: ₹" + comparison.getTotalExtraCharge());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/fleet/import-xml")
    public String importXmlForm(Model model) {
        model.addAttribute("adminTab", "import-xml");
        return "admin/import-xml";
    }

    @PostMapping("/fleet/import-xml")
    public String handleXmlImport(@RequestParam("file") MultipartFile file,
                                  Model model) {
        if (file.isEmpty()) {
            model.addAttribute("errorMessage", "Please select a valid XML file to upload.");
            model.addAttribute("adminTab", "import-xml");
            return "admin/import-xml";
        }

        try {
            FleetImportService.FleetImportResult result = fleetImportService.importFleetFromXml(file.getInputStream());
            model.addAttribute("importResult", result);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to process XML file: " + e.getMessage());
        }

        model.addAttribute("adminTab", "import-xml");
        return "admin/import-xml";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("adminTab", "reports");
        return "admin/reports";
    }

    @GetMapping("/customers")
    public String customers(Model model) {
        List<User> customers = userRepository.findAll();
        model.addAttribute("customers", customers);
        model.addAttribute("adminTab", "customers");
        return "admin/customers";
    }
}
