package com.drivesense.dto.xml;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "fleet")
@XmlAccessorType(XmlAccessType.FIELD)
public class FleetXml {

    @XmlElement(name = "car")
    private List<CarImportItemXml> cars = new ArrayList<>();

    public List<CarImportItemXml> getCars() {
        return cars;
    }

    public void setCars(List<CarImportItemXml> cars) {
        this.cars = cars;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CarImportItemXml {
        @XmlElement(name = "brand", required = true)
        private String brand;

        @XmlElement(name = "model", required = true)
        private String model;

        @XmlElement(name = "regNumber", required = true)
        private String regNumber;

        @XmlElement(name = "type", required = true)
        private String type;

        @XmlElement(name = "fuelType", required = true)
        private String fuelType;

        @XmlElement(name = "transmission", required = true)
        private String transmission;

        @XmlElement(name = "seats", required = true)
        private int seats;

        @XmlElement(name = "bootSize", required = true)
        private String bootSize;

        @XmlElement(name = "pricePerDay", required = true)
        private BigDecimal pricePerDay;

        @XmlElement(name = "pricePerHour")
        private BigDecimal pricePerHour;

        @XmlElement(name = "kmPerDayLimit")
        private Integer kmPerDayLimit;

        @XmlElement(name = "extraKmRate")
        private BigDecimal extraKmRate;

        @XmlElement(name = "co2GPerKm")
        private Integer co2GPerKm;

        @XmlElement(name = "imageUrl")
        private String imageUrl;

        @XmlElementWrapper(name = "features")
        @XmlElement(name = "feature")
        private List<String> features = new ArrayList<>();

        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public String getRegNumber() { return regNumber; }
        public void setRegNumber(String regNumber) { this.regNumber = regNumber; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getFuelType() { return fuelType; }
        public void setFuelType(String fuelType) { this.fuelType = fuelType; }

        public String getTransmission() { return transmission; }
        public void setTransmission(String transmission) { this.transmission = transmission; }

        public int getSeats() { return seats; }
        public void setSeats(int seats) { this.seats = seats; }

        public String getBootSize() { return bootSize; }
        public void setBootSize(String bootSize) { this.bootSize = bootSize; }

        public BigDecimal getPricePerDay() { return pricePerDay; }
        public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }

        public BigDecimal getPricePerHour() { return pricePerHour; }
        public void setPricePerHour(BigDecimal pricePerHour) { this.pricePerHour = pricePerHour; }

        public Integer getKmPerDayLimit() { return kmPerDayLimit; }
        public void setKmPerDayLimit(Integer kmPerDayLimit) { this.kmPerDayLimit = kmPerDayLimit; }

        public BigDecimal getExtraKmRate() { return extraKmRate; }
        public void setExtraKmRate(BigDecimal extraKmRate) { this.extraKmRate = extraKmRate; }

        public Integer getCo2GPerKm() { return co2GPerKm; }
        public void setCo2GPerKm(Integer co2GPerKm) { this.co2GPerKm = co2GPerKm; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public List<String> getFeatures() { return features; }
        public void setFeatures(List<String> features) { this.features = features; }
    }
}
