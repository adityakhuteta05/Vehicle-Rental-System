package com.drivesense.entity;

import com.drivesense.enums.TrustTier;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "licence_no", length = 30)
    private String licenceNo;

    private LocalDate dob;

    @Column(nullable = false, length = 20)
    private String role; // "ROLE_RENTER", "ROLE_OWNER", "ROLE_ADMIN" (or legacy "ROLE_CUSTOMER")

    @Column(name = "trust_score", nullable = false)
    private int trustScore = 50;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(name = "verification_status", length = 20)
    private String verificationStatus = "VERIFIED"; // PENDING, VERIFIED, REJECTED

    @Column(name = "government_id", length = 50)
    private String governmentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public User(String fullName, String email, String phone, String passwordHash, String licenceNo, LocalDate dob, String role) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.licenceNo = licenceNo;
        this.dob = dob;
        this.role = role;
        this.trustScore = 50;
        this.verificationStatus = "VERIFIED";
        this.createdAt = LocalDateTime.now();
    }

    public TrustTier getTrustTier() {
        return TrustTier.fromScore(this.trustScore);
    }

    public boolean isRenter() {
        return "ROLE_RENTER".equals(role) || "ROLE_CUSTOMER".equals(role);
    }

    public boolean isOwner() {
        return "ROLE_OWNER".equals(role);
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(role);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getLicenceNo() {
        return licenceNo;
    }

    public void setLicenceNo(String licenceNo) {
        this.licenceNo = licenceNo;
    }

    public String getDrivingLicenceNumber() {
        return licenceNo;
    }

    public void setDrivingLicenceNumber(String drivingLicenceNumber) {
        this.licenceNo = drivingLicenceNumber;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(int trustScore) {
        this.trustScore = Math.max(0, Math.min(100, trustScore));
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getGovernmentId() {
        return governmentId;
    }

    public void setGovernmentId(String governmentId) {
        this.governmentId = governmentId;
    }
}
