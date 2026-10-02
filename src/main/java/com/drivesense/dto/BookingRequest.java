package com.drivesense.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.drivesense.enums.TripType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class BookingRequest {

    private Long carId;
    private Long pickupLocationId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime endTime;

    private TripType tripType;
    private boolean driverRequired;
    private boolean insuranceRequired = true;
    private boolean childSeatRequired;

    private String paymentMethod = "CARD";
    private String cardHolderName;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvv;
    private String upiId;

    public BookingRequest() {
        this.startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        this.endTime = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0);
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public Long getPickupLocationId() {
        return pickupLocationId;
    }

    public void setPickupLocationId(Long pickupLocationId) {
        this.pickupLocationId = pickupLocationId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public boolean isDriverRequired() {
        return driverRequired;
    }

    public void setDriverRequired(boolean driverRequired) {
        this.driverRequired = driverRequired;
    }

    public boolean isInsuranceRequired() {
        return insuranceRequired;
    }

    public void setInsuranceRequired(boolean insuranceRequired) {
        this.insuranceRequired = insuranceRequired;
    }

    public boolean isChildSeatRequired() {
        return childSeatRequired;
    }

    public void setChildSeatRequired(boolean childSeatRequired) {
        this.childSeatRequired = childSeatRequired;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }

    public String getCardCvv() {
        return cardCvv;
    }

    public void setCardCvv(String cardCvv) {
        this.cardCvv = cardCvv;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }
}
