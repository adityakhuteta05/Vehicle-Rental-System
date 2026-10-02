package com.drivesense.controller;

import com.drivesense.entity.Car;
import com.drivesense.enums.TripType;
import com.drivesense.service.CarService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final CarService carService;

    public HomeController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Car> featuredCars = carService.getFeaturedCars();
        model.addAttribute("featuredCars", featuredCars);
        model.addAttribute("tripTypes", TripType.values());
        model.addAttribute("activeNav", "home");
        return "index";
    }

    @GetMapping("/how-it-works")
    public String howItWorks(Model model) {
        model.addAttribute("activeNav", "how-it-works");
        return "how-it-works";
    }
}
