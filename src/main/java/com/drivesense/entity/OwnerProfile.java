package com.drivesense.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "owner_profiles")
public class OwnerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_name", length = 120)
    private String businessName;

    @Column(name = "government_id", length = 50)
    private String governmentId;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(name = "verification_status", length = 20)
    private String verificationStatus = "VERIFIED"; // PENDING, VERIFIED, REJECTED

    @Column(name = "owner_rating", precision = 2, scale = 1)
    private BigDecimal ownerRating = new BigDecimal("4.9");

    @Column(name = "total_earnings", precision = 12, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public OwnerProfile() {}

    public OwnerProfile(User user, String businessName, String governmentId, String address, String city) {
        this.user = user;
        this.businessName = businessName;
        this.governmentId = governmentId;
        this.address = address;
        this.city = city;
        this.verificationStatus = "VERIFIED";
        this.ownerRating = new BigDecimal("4.9");
        this.totalEarnings = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getGovernmentId() {
        return governmentId;
    }

    public void setGovernmentId(String governmentId) {
        this.governmentId = governmentId;
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

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public BigDecimal getOwnerRating() {
        return ownerRating;
    }

    public void setOwnerRating(BigDecimal ownerRating) {
        this.ownerRating = ownerRating;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
