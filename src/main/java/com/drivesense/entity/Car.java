package com.drivesense.entity;

import com.drivesense.enums.BootSize;
import com.drivesense.enums.CarStatus;
import com.drivesense.enums.CarType;
import com.drivesense.enums.FuelType;
import com.drivesense.enums.Transmission;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cars", indexes = {
    @Index(name = "idx_car_reg", columnList = "reg_number"),
    @Index(name = "idx_car_type", columnList = "type"),
    @Index(name = "idx_car_status", columnList = "status")
})
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String brand;

    @Column(nullable = false, length = 60)
    private String model;

    @Column(name = "reg_number", nullable = false, unique = true, length = 25)
    private String regNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "model_year")
    private Integer year = 2024;

    @Column(name = "security_deposit", precision = 10, scale = 2)
    private BigDecimal securityDeposit = new BigDecimal("3000.00");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CarType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 15)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Transmission transmission;

    @Column(nullable = false)
    private int seats;

    @Enumerated(EnumType.STRING)
    @Column(name = "boot_size", nullable = false, length = 5)
    private BootSize bootSize;

    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Column(name = "price_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(name = "km_per_day_limit", nullable = false)
    private int kmPerDayLimit = 300;

    @Column(name = "extra_km_rate", nullable = false, precision = 6, scale = 2)
    private BigDecimal extraKmRate = new BigDecimal("12.00");

    @Column(name = "co2_g_per_km", nullable = false)
    private int co2GPerKm = 140;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CarStatus status = CarStatus.AVAILABLE;

    @Column(name = "avg_rating", precision = 2, scale = 1)
    private BigDecimal avgRating = new BigDecimal("4.8");

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "car_features", joinColumns = @JoinColumn(name = "car_id"))
    @Column(name = "feature")
    private List<String> features = new ArrayList<>();

    public Car() {}

    public Car(String brand, String model, String regNumber, CarType type, FuelType fuelType,
               Transmission transmission, int seats, BootSize bootSize, BigDecimal pricePerDay,
               BigDecimal pricePerHour, int kmPerDayLimit, BigDecimal extraKmRate, int co2GPerKm,
               String imageUrl, CarStatus status, BigDecimal avgRating) {
        this.brand = brand;
        this.model = model;
        this.regNumber = regNumber;
        this.type = type;
        this.fuelType = fuelType;
        this.transmission = transmission;
        this.seats = seats;
        this.bootSize = bootSize;
        this.pricePerDay = pricePerDay;
        this.pricePerHour = pricePerHour;
        this.kmPerDayLimit = kmPerDayLimit;
        this.extraKmRate = extraKmRate;
        this.co2GPerKm = co2GPerKm;
        this.imageUrl = imageUrl;
        this.status = status;
        this.avgRating = avgRating;
    }

    public String getDisplayName() {
        return brand + " " + model;
    }

    public boolean isEcoFriendly() {
        return fuelType == FuelType.EV || fuelType == FuelType.CNG || co2GPerKm <= 110;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public CarType getType() {
        return type;
    }

    public void setType(CarType type) {
        this.type = type;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public Transmission getTransmission() {
        return transmission;
    }

    public void setTransmission(Transmission transmission) {
        this.transmission = transmission;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public BootSize getBootSize() {
        return bootSize;
    }

    public void setBootSize(BootSize bootSize) {
        this.bootSize = bootSize;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public int getKmPerDayLimit() {
        return kmPerDayLimit;
    }

    public void setKmPerDayLimit(int kmPerDayLimit) {
        this.kmPerDayLimit = kmPerDayLimit;
    }

    public BigDecimal getExtraKmRate() {
        return extraKmRate;
    }

    public void setExtraKmRate(BigDecimal extraKmRate) {
        this.extraKmRate = extraKmRate;
    }

    public int getCo2GPerKm() {
        return co2GPerKm;
    }

    public void setCo2GPerKm(int co2GPerKm) {
        this.co2GPerKm = co2GPerKm;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public CarStatus getStatus() {
        return status;
    }

    public void setStatus(CarStatus status) {
        this.status = status;
    }

    public BigDecimal getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(BigDecimal avgRating) {
        this.avgRating = avgRating;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getSecurityDeposit() {
        return securityDeposit;
    }

    public void setSecurityDeposit(BigDecimal securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    public int getReviewCount() {
        return 18;
    }
}
