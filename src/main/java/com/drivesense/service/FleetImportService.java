package com.drivesense.service;

import com.drivesense.dto.xml.FleetXml;
import com.drivesense.entity.Car;
import com.drivesense.enums.BootSize;
import com.drivesense.enums.CarStatus;
import com.drivesense.enums.CarType;
import com.drivesense.enums.FuelType;
import com.drivesense.enums.Transmission;
import com.drivesense.repository.CarRepository;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class FleetImportService {

    private static final Logger log = LoggerFactory.getLogger(FleetImportService.class);

    private final CarRepository carRepository;

    public FleetImportService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public static class FleetImportResult {
        private int importedCount = 0;
        private int skippedCount = 0;
        private final List<String> errors = new ArrayList<>();
        private final List<String> successMessages = new ArrayList<>();

        public int getImportedCount() { return importedCount; }
        public int getSkippedCount() { return skippedCount; }
        public List<String> getErrors() { return errors; }
        public List<String> getSuccessMessages() { return successMessages; }
        public boolean isSuccess() { return errors.isEmpty(); }
    }

    @Transactional
    public FleetImportResult importFleetFromXml(InputStream xmlInputStream) {
        FleetImportResult result = new FleetImportResult();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(FleetXml.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            FleetXml fleet = (FleetXml) unmarshaller.unmarshal(xmlInputStream);

            if (fleet == null || fleet.getCars() == null || fleet.getCars().isEmpty()) {
                result.getErrors().add("The uploaded XML does not contain any <car> elements.");
                return result;
            }

            int index = 0;
            for (FleetXml.CarImportItemXml item : fleet.getCars()) {
                index++;
                try {
                    validateAndSaveCar(item, result);
                } catch (Exception ex) {
                    result.skippedCount++;
                    result.getErrors().add("Item #" + index + " (" + item.getBrand() + " " + item.getModel() + "): " + ex.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("XML fleet import error", e);
            result.getErrors().add("Malformed XML content: " + e.getMessage());
        }

        return result;
    }

    private void validateAndSaveCar(FleetXml.CarImportItemXml item, FleetImportResult result) {
        if (item.getRegNumber() == null || item.getRegNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number is required");
        }
        if (carRepository.findByRegNumber(item.getRegNumber().trim()).isPresent()) {
            throw new IllegalArgumentException("Car with reg " + item.getRegNumber() + " already exists in fleet");
        }
        if (item.getPricePerDay() == null || item.getPricePerDay().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid price per day");
        }

        CarType carType;
        try {
            carType = CarType.valueOf(item.getType().toUpperCase());
        } catch (Exception e) {
            carType = CarType.SEDAN;
        }

        FuelType fuelType;
        try {
            fuelType = FuelType.valueOf(item.getFuelType().toUpperCase());
        } catch (Exception e) {
            fuelType = FuelType.PETROL;
        }

        Transmission transmission;
        try {
            transmission = Transmission.valueOf(item.getTransmission().toUpperCase());
        } catch (Exception e) {
            transmission = Transmission.AUTOMATIC;
        }

        BootSize bootSize;
        try {
            bootSize = BootSize.valueOf(item.getBootSize().toUpperCase());
        } catch (Exception e) {
            bootSize = BootSize.M;
        }

        BigDecimal pricePerHour = item.getPricePerHour() != null ? item.getPricePerHour()
                : item.getPricePerDay().divide(new BigDecimal("15"), 2, BigDecimal.ROUND_HALF_UP);
        int kmLimit = item.getKmPerDayLimit() != null ? item.getKmPerDayLimit() : 300;
        BigDecimal extraKmRate = item.getExtraKmRate() != null ? item.getExtraKmRate() : new BigDecimal("15.00");
        int co2 = item.getCo2GPerKm() != null ? item.getCo2GPerKm() : fuelType.getDefaultCo2PerKm();

        String imageUrl = item.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80";
        }

        Car car = new Car(
                item.getBrand(), item.getModel(), item.getRegNumber().trim(),
                carType, fuelType, transmission, Math.max(2, item.getSeats()),
                bootSize, item.getPricePerDay(), pricePerHour, kmLimit, extraKmRate,
                co2, imageUrl, CarStatus.AVAILABLE, new BigDecimal("4.8")
        );

        if (item.getFeatures() != null && !item.getFeatures().isEmpty()) {
            car.setFeatures(item.getFeatures());
        }

        carRepository.save(car);
        result.importedCount++;
        result.getSuccessMessages().add("Successfully added " + car.getDisplayName() + " (" + car.getRegNumber() + ")");
    }
}
