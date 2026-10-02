package com.drivesense.service;

import com.drivesense.entity.User;
import com.drivesense.exception.ResourceNotFoundException;
import com.drivesense.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerCustomer(String fullName, String email, String phone, String rawPassword,
                                 String licenceNo, LocalDate dob) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (userRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        // AU-4: Customer must be 18+ and provide licence number
        if (dob == null || Period.between(dob, LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("You must be at least 18 years of age to register and rent a car.");
        }
        if (licenceNo == null || licenceNo.trim().isEmpty()) {
            throw new IllegalArgumentException("A valid driving licence number is mandatory for renting.");
        }

        User user = new User(
                fullName.trim(),
                email.trim().toLowerCase(),
                phone != null ? phone.trim() : null,
                passwordEncoder.encode(rawPassword),
                licenceNo.trim(),
                dob,
                "ROLE_RENTER"
        );
        user.setTrustScore(50); // Starting score TS-1

        return userRepository.save(user);
    }

    @Transactional
    public User registerOwner(String fullName, String email, String phone, String rawPassword,
                              String governmentId, String address, String city, LocalDate dob) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (userRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        User user = new User(
                fullName.trim(),
                email.trim().toLowerCase(),
                phone != null ? phone.trim() : null,
                passwordEncoder.encode(rawPassword),
                null,
                dob,
                "ROLE_OWNER"
        );
        user.setAddress(address);
        user.setCity(city);
        user.setGovernmentId(governmentId);
        user.setVerificationStatus("VERIFIED");
        user.setTrustScore(100);

        return userRepository.save(user);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateProfile(Long userId, String fullName, String phone, String licenceNo) {
        User user = getById(userId);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setLicenceNo(licenceNo);
        return userRepository.save(user);
    }
}
