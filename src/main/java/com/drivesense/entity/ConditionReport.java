package com.drivesense.entity;

import com.drivesense.enums.ReportStage;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "condition_reports", indexes = {
    @Index(name = "idx_report_booking_stage", columnList = "booking_id, stage")
})
public class ConditionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ReportStage stage; // PICKUP or RETURN

    @Column(name = "fuel_percent", nullable = false)
    private int fuelPercent = 100;

    @Column(name = "odometer_km", nullable = false)
    private int odometerKm = 0;

    @Column(name = "damage_zones", length = 255)
    private String damageZones = ""; // e.g., "FRONT,LEFT"

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "photo_urls", columnDefinition = "TEXT")
    private String photoUrls;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public ConditionReport() {}

    public ConditionReport(Booking booking, ReportStage stage, int fuelPercent, int odometerKm, String damageZones, String notes) {
        this.booking = booking;
        this.stage = stage;
        this.fuelPercent = fuelPercent;
        this.odometerKm = odometerKm;
        this.damageZones = damageZones != null ? damageZones : "";
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    public List<String> getDamageZoneList() {
        if (damageZones == null || damageZones.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(damageZones.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public boolean hasZoneDamage(String zone) {
        return getDamageZoneList().contains(zone);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public ReportStage getStage() {
        return stage;
    }

    public void setStage(ReportStage stage) {
        this.stage = stage;
    }

    public int getFuelPercent() {
        return fuelPercent;
    }

    public void setFuelPercent(int fuelPercent) {
        this.fuelPercent = fuelPercent;
    }

    public int getOdometerKm() {
        return odometerKm;
    }

    public void setOdometerKm(int odometerKm) {
        this.odometerKm = odometerKm;
    }

    public String getDamageZones() {
        return damageZones;
    }

    public void setDamageZones(String damageZones) {
        this.damageZones = damageZones;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(String photoUrls) {
        this.photoUrls = photoUrls;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
