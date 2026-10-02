package com.drivesense.controller;

import com.drivesense.dto.VibeMatchResult;
import com.drivesense.dto.VibeRequest;
import com.drivesense.enums.BootSize;
import com.drivesense.enums.TripType;
import com.drivesense.service.VibeMatchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/vibe-match")
public class VibeMatchController {

    private final VibeMatchService vibeMatchService;

    public VibeMatchController(VibeMatchService vibeMatchService) {
        this.vibeMatchService = vibeMatchService;
    }

    @GetMapping
    public String showWizard(@RequestParam(required = false) TripType tripType, Model model) {
        VibeRequest request = new VibeRequest();
        if (tripType != null) {
            request.setTripType(tripType);
        }
        model.addAttribute("vibeRequest", request);
        model.addAttribute("tripTypes", TripType.values());
        model.addAttribute("bootSizes", BootSize.values());
        model.addAttribute("activeNav", "vibe-match");
        return "vibe-match";
    }

    @PostMapping
    public String calculateVibeMatch(@ModelAttribute VibeRequest vibeRequest, Model model) {
        List<VibeMatchResult> matches = vibeMatchService.matchCars(vibeRequest);

        model.addAttribute("vibeRequest", vibeRequest);
        model.addAttribute("matches", matches);
        model.addAttribute("tripTypes", TripType.values());
        model.addAttribute("bootSizes", BootSize.values());
        model.addAttribute("activeNav", "vibe-match");
        model.addAttribute("hasCalculated", true);

        return "vibe-match";
    }
}
