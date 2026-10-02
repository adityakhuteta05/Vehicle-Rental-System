package com.drivesense.controller;

import com.drivesense.entity.User;
import com.drivesense.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out securely.");
        }
        model.addAttribute("loginRole", "RENTER");
        return "auth/login";
    }

    @GetMapping("/renter/login")
    public String renterLoginRedirect() {
        return "redirect:/login";
    }

    @GetMapping("/owner/login")
    public String ownerLoginPage(@RequestParam(required = false) String error,
                                 @RequestParam(required = false) String logout,
                                 Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid owner credentials. Please check your email and password.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been signed out from Fleet Host Command.");
        }
        model.addAttribute("loginRole", "OWNER");
        return "auth/owner-login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam String fullName,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String phone,
                                 @RequestParam String password,
                                 @RequestParam String licenceNo,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.registerCustomer(fullName, email, phone, password, licenceNo, dob);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Account created successfully with a starting Trust Score of 50! Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/owner/register")
    public String ownerRegisterPage(Model model) {
        return "auth/owner-register";
    }

    @PostMapping("/owner/register")
    public String handleOwnerRegister(@RequestParam String fullName,
                                      @RequestParam String email,
                                      @RequestParam(required = false) String phone,
                                      @RequestParam String password,
                                      @RequestParam String governmentId,
                                      @RequestParam(required = false) String address,
                                      @RequestParam(required = false) String city,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
                                      RedirectAttributes redirectAttributes) {
        try {
            userService.registerOwner(fullName, email, phone, password, governmentId, address, city, dob != null ? dob : LocalDate.of(1990, 1, 1));
            redirectAttributes.addFlashAttribute("successMessage",
                    "Vehicle Owner account registered successfully! Please log in to access your Fleet Dashboard.");
            return "redirect:/owner/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/owner/register";
        }
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getByEmail(userDetails.getUsername());
        if (user.isOwner()) {
            return "redirect:/owner/profile";
        }
        return "redirect:/renter/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String fullName,
                                @RequestParam(required = false) String phone,
                                @RequestParam String licenceNo,
                                RedirectAttributes redirectAttributes) {
        User user = userService.getByEmail(userDetails.getUsername());
        userService.updateProfile(user.getId(), fullName, phone, licenceNo);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        return "redirect:/profile";
    }
}
