package com.drivesense.service;

import com.drivesense.dto.BookingRequest;
import com.drivesense.entity.Booking;
import com.drivesense.entity.Car;
import com.drivesense.entity.User;
import com.drivesense.enums.BootSize;
import com.drivesense.enums.CarStatus;
import com.drivesense.enums.CarType;
import com.drivesense.enums.FuelType;
import com.drivesense.enums.Transmission;
import com.drivesense.exception.CarNotAvailableException;
import com.drivesense.repository.BookingRepository;
import com.drivesense.repository.CarRepository;
import com.drivesense.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Concurrency Safe: Two simultaneous parallel booking requests for the same slot result in exactly one success and zero double bookings")
    public void testSimultaneousDoubleBookingPrevention() throws InterruptedException {
        // Setup isolated test entities
        User user1 = userRepository.save(new User("User One", "user1_concurrent@test.com", "111", "pw", "DL01", LocalDate.of(1996, 1, 1), "ROLE_CUSTOMER"));
        User user2 = userRepository.save(new User("User Two", "user2_concurrent@test.com", "222", "pw", "DL02", LocalDate.of(1996, 2, 2), "ROLE_CUSTOMER"));

        Car car = new Car("ConcurrencyTest", "Sedan", "CONC-9999", CarType.SEDAN, FuelType.PETROL,
                Transmission.AUTOMATIC, 5, BootSize.M, new BigDecimal("2500"), new BigDecimal("150"),
                300, new BigDecimal("12"), 130, "img.jpg", CarStatus.AVAILABLE, new BigDecimal("4.8"));
        car = carRepository.save(car);

        final Long carId = car.getId();
        final LocalDateTime start = LocalDateTime.now().plusDays(20).withHour(10).withMinute(0);
        final LocalDateTime end = start.plusDays(2);

        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final User user = (i == 0) ? user1 : user2;
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    // Wait for both threads to be ready to execute simultaneously
                    startLatch.await();

                    BookingRequest req = new BookingRequest();
                    req.setCarId(carId);
                    req.setStartTime(start);
                    req.setEndTime(end);

                    bookingService.createBooking(user, req);
                    successCount.incrementAndGet();

                } catch (CarNotAvailableException e) {
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    // unexpected error
                    System.err.println("Unexpected concurrency error: " + e.getMessage());
                }
            });
        }

        readyLatch.await();
        // Fire both threads simultaneously
        startLatch.countDown();

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // Verification of concurrency guarantees (PRD Section 12 & 19)
        assertEquals(1, successCount.get(), "Exactly one booking must succeed");
        assertEquals(1, conflictCount.get(), "Exactly one booking must be rejected with CarNotAvailableException");

        long countOverlaps = bookingRepository.countOverlaps(carId, start, end);
        assertEquals(1, countOverlaps, "Database must contain exactly 1 confirmed reservation for that timeslot (zero double booking)");
    }
}
