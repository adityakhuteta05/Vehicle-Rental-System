package com.drivesense.service;

import com.drivesense.entity.Booking;
import com.drivesense.entity.Car;
import com.drivesense.enums.CarStatus;
import com.drivesense.enums.CarType;
import com.drivesense.enums.FuelType;
import com.drivesense.enums.Transmission;
import com.drivesense.exception.ResourceNotFoundException;
import com.drivesense.repository.BookingRepository;
import com.drivesense.repository.CarRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;

    public CarService(CarRepository carRepository, BookingRepository bookingRepository) {
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Car> getFeaturedCars() {
        return carRepository.findTop6ByStatusOrderByAvgRatingDesc(CarStatus.AVAILABLE);
    }

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Page<Car> searchCars(CarType type, FuelType fuelType, Transmission transmission,
                               Integer minSeats, BigDecimal maxPrice, String query,
                               String sortBy, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "pricePerDay");
        if ("price_desc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "pricePerDay");
        } else if ("rating".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "avgRating");
        } else if ("seats".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "seats");
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), size, sort);
        return carRepository.searchCars(CarStatus.AVAILABLE, type, fuelType, transmission, minSeats, maxPrice, query, pageable);
    }

    public Car getCarById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found: " + id));
    }

    @Transactional
    public Car saveCar(Car car) {
        return carRepository.save(car);
    }

    @Transactional
    public Car updateStatus(Long id, CarStatus newStatus) {
        Car car = getCarById(id);
        car.setStatus(newStatus);
        return carRepository.save(car);
    }

    @Transactional
    public void deleteCar(Long id) {
        carRepository.deleteById(id);
    }

    public List<Map<String, String>> getBookedDateRanges(Long carId) {
        List<Booking> bookings = bookingRepository.findUpcomingOrActiveByCar(carId, LocalDateTime.now().minusDays(1));
        List<Map<String, String>> ranges = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (Booking b : bookings) {
            Map<String, String> map = new HashMap<>();
            map.put("start", b.getStartTime().format(fmt));
            map.put("end", b.getEndTime().format(fmt));
            map.put("code", b.getBookingCode());
            ranges.add(map);
        }
        return ranges;
    }
}
