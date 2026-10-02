package com.drivesense.controller;

import com.drivesense.entity.Car;
import com.drivesense.entity.Location;
import com.drivesense.entity.Review;
import com.drivesense.enums.CarType;
import com.drivesense.enums.FuelType;
import com.drivesense.enums.Transmission;
import com.drivesense.repository.LocationRepository;
import com.drivesense.service.CarService;
import com.drivesense.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;
    private final ReviewService reviewService;
    private final LocationRepository locationRepository;

    public CarController(CarService carService, ReviewService reviewService, LocationRepository locationRepository) {
        this.carService = carService;
        this.reviewService = reviewService;
        this.locationRepository = locationRepository;
    }

    @GetMapping
    public String catalogue(
            @RequestParam(required = false) CarType type,
            @RequestParam(required = false) FuelType fuelType,
            @RequestParam(required = false) Transmission transmission,
            @RequestParam(required = false) Integer minSeats,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "price_asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Car> carPage = carService.searchCars(type, fuelType, transmission, minSeats, maxPrice, query, sort, page, 9);

        model.addAttribute("carPage", carPage);
        model.addAttribute("cars", carPage.getContent());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedFuel", fuelType);
        model.addAttribute("selectedTransmission", transmission);
        model.addAttribute("selectedSeats", minSeats);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("query", query);
        model.addAttribute("sort", sort);

        model.addAttribute("carTypes", CarType.values());
        model.addAttribute("fuelTypes", FuelType.values());
        model.addAttribute("transmissions", Transmission.values());
        model.addAttribute("activeNav", "catalogue");

        return "catalogue";
    }

    @GetMapping("/{id}")
    public String carDetails(@PathVariable Long id, Model model) {
        Car car = carService.getCarById(id);
        List<Review> reviews = reviewService.getReviewsForCar(id);
        List<Location> locations = locationRepository.findAll();

        model.addAttribute("car", car);
        model.addAttribute("reviews", reviews);
        model.addAttribute("locations", locations);
        model.addAttribute("activeNav", "catalogue");

        return "car-details";
    }

    @GetMapping("/compare")
    public String compareCars(@RequestParam(required = false) Long car1Id,
                              @RequestParam(required = false) Long car2Id,
                              Model model) {
        List<Car> allCars = carService.getAllCars();
        model.addAttribute("allCars", allCars);

        Car car1 = (car1Id != null) ? carService.getCarById(car1Id) : (!allCars.isEmpty() ? allCars.get(0) : null);
        Car car2 = (car2Id != null) ? carService.getCarById(car2Id) : (allCars.size() > 1 ? allCars.get(1) : null);

        model.addAttribute("car1", car1);
        model.addAttribute("car2", car2);
        model.addAttribute("activeNav", "catalogue");

        return "compare";
    }
}
