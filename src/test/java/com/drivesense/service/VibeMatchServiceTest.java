package com.drivesense.service;

import com.drivesense.dto.VibeMatchResult;
import com.drivesense.dto.VibeRequest;
import com.drivesense.entity.Car;
import com.drivesense.enums.BootSize;
import com.drivesense.enums.CarStatus;
import com.drivesense.enums.CarType;
import com.drivesense.enums.FuelType;
import com.drivesense.enums.Transmission;
import com.drivesense.enums.TripType;
import com.drivesense.repository.BookingRepository;
import com.drivesense.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class VibeMatchServiceTest {

    private CarRepository carRepository;
    private BookingRepository bookingRepository;
    private VibeMatchService vibeMatchService;

    @BeforeEach
    public void setup() {
        carRepository = Mockito.mock(CarRepository.class);
        bookingRepository = Mockito.mock(BookingRepository.class);
        vibeMatchService = new VibeMatchService(carRepository, bookingRepository);
    }

    @Test
    @DisplayName("Vibe Match returns at most 3 cars ordered by highest match score")
    public void testVibeMatchRanking() {
        Car car1 = new Car("Hyundai", "Creta", "REG1", CarType.SUV, FuelType.PETROL, Transmission.AUTOMATIC, 5, BootSize.M,
                new BigDecimal("2900"), new BigDecimal("165"), 300, new BigDecimal("12"), 140, "img", CarStatus.AVAILABLE, new BigDecimal("4.8"));
        car1.setId(1L);

        Car car2 = new Car("Tata", "Nexon EV", "REG2", CarType.EV, FuelType.EV, Transmission.AUTOMATIC, 5, BootSize.M,
                new BigDecimal("3200"), new BigDecimal("180"), 300, new BigDecimal("12"), 0, "img", CarStatus.AVAILABLE, new BigDecimal("4.9"));
        car2.setId(2L);

        Car car3 = new Car("Maruti", "Swift", "REG3", CarType.HATCHBACK, FuelType.PETROL, Transmission.MANUAL, 5, BootSize.S,
                new BigDecimal("1600"), new BigDecimal("90"), 300, new BigDecimal("10"), 115, "img", CarStatus.AVAILABLE, new BigDecimal("4.5"));
        car3.setId(3L);

        Car car4 = new Car("Toyota", "Fortuner", "REG4", CarType.SUV, FuelType.DIESEL, Transmission.AUTOMATIC, 7, BootSize.L,
                new BigDecimal("5800"), new BigDecimal("320"), 300, new BigDecimal("18"), 190, "img", CarStatus.AVAILABLE, new BigDecimal("4.9"));
        car4.setId(4L);

        when(carRepository.findByStatus(CarStatus.AVAILABLE)).thenReturn(Arrays.asList(car1, car2, car3, car4));
        when(bookingRepository.countOverlaps(any(), any(), any())).thenReturn(0L);

        VibeRequest req = new VibeRequest();
        req.setTripType(TripType.HILL_DRIVE);
        req.setPassengers(5);
        req.setLuggage(BootSize.M);
        req.setMaxDailyBudget(new BigDecimal("4000"));
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(3));

        List<VibeMatchResult> results = vibeMatchService.matchCars(req);

        assertNotNull(results);
        assertTrue(results.size() <= 3, "Result count should not exceed 3");
        // Verify scores are in descending order
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getMatchPercent() >= results.get(i + 1).getMatchPercent());
        }
        // Verify reasons are present
        assertFalse(results.get(0).getReason().isEmpty());
    }
}
