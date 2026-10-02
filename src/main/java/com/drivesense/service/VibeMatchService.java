package com.drivesense.service;

import com.drivesense.dto.VibeMatchResult;
import com.drivesense.dto.VibeRequest;
import com.drivesense.entity.Car;
import com.drivesense.enums.BootSize;
import com.drivesense.enums.CarStatus;
import com.drivesense.enums.CarType;
import com.drivesense.enums.TripType;
import com.drivesense.repository.BookingRepository;
import com.drivesense.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VibeMatchService {

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;

    public VibeMatchService(CarRepository carRepository, BookingRepository bookingRepository) {
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<VibeMatchResult> matchCars(VibeRequest request) {
        List<Car> availableCars = carRepository.findByStatus(CarStatus.AVAILABLE);

        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();

        return availableCars.stream()
                // VM-3: Only available cars for the chosen dates are returned
                .filter(car -> {
                    if (start != null && end != null && end.isAfter(start)) {
                        return bookingRepository.countOverlaps(car.getId(), start, end) == 0;
                    }
                    return true;
                })
                // Calculate match score using Java Streams
                .map(car -> scoreCar(car, request))
                // Sort by highest match score descending
                .sorted(Comparator.comparingInt(VibeMatchResult::getMatchPercent).reversed())
                // VM-2: Output top 3 cars with percentage and reason
                .limit(3)
                .collect(Collectors.toList());
    }

    private VibeMatchResult scoreCar(Car car, VibeRequest req) {
        // 1. seatFit: 100 if seats >= passengers and seats - passengers <= 2; reduces if too many empty seats; 0 if too few
        double seatFit;
        int diff = car.getSeats() - req.getPassengers();
        if (diff < 0) {
            seatFit = 0.0;
        } else if (diff <= 2) {
            seatFit = 100.0;
        } else {
            seatFit = Math.max(20.0, 100.0 - (diff - 2) * 15.0);
        }

        // 2. luggageFit: Boot size category (S/M/L) vs requested
        double luggageFit;
        int bootRank = getBootRank(car.getBootSize());
        int reqRank = getBootRank(req.getLuggage());
        if (bootRank == reqRank) {
            luggageFit = 100.0;
        } else if (bootRank > reqRank) {
            luggageFit = 85.0; // plenty of extra space
        } else {
            luggageFit = Math.max(10.0, 100.0 - (reqRank - bootRank) * 45.0);
        }

        // 3. budgetFit: 100 if price <= budget, linear drop up to 30% over budget, 0 beyond
        double budgetFit;
        BigDecimal dailyPrice = car.getPricePerDay();
        BigDecimal budget = req.getMaxDailyBudget();
        if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) {
            budgetFit = 100.0;
        } else if (dailyPrice.compareTo(budget) <= 0) {
            budgetFit = 100.0;
        } else {
            BigDecimal maxAllowed = budget.multiply(new BigDecimal("1.30"));
            if (dailyPrice.compareTo(maxAllowed) > 0) {
                budgetFit = 0.0;
            } else {
                double excess = dailyPrice.subtract(budget).doubleValue();
                double allowedBand = budget.multiply(new BigDecimal("0.30")).doubleValue();
                budgetFit = Math.max(0.0, 100.0 - (excess / allowedBand) * 100.0);
            }
        }

        // 4. tripTypeFit: Lookup matrix
        double tripTypeFit = computeTripTypeFit(car.getType(), req.getTripType());

        // 5. ratingScore: Average rating / 5 * 100
        double ratingVal = car.getAvgRating() != null ? car.getAvgRating().doubleValue() : 4.5;
        double ratingScore = Math.min(100.0, (ratingVal / 5.0) * 100.0);

        // Weighted algorithm from PRD section 10:
        // score = 0.35 x seatFit + 0.20 x luggageFit + 0.20 x budgetFit + 0.15 x tripTypeFit + 0.10 x ratingScore
        double compositeScore = (0.35 * seatFit)
                + (0.20 * luggageFit)
                + (0.20 * budgetFit)
                + (0.15 * tripTypeFit)
                + (0.10 * ratingScore);

        int finalScore = (int) Math.round(Math.min(99, Math.max(30, compositeScore)));

        String reason = generateReason(car, req, diff, bootRank, reqRank);

        return new VibeMatchResult(car, finalScore, reason, seatFit, luggageFit, budgetFit, tripTypeFit);
    }

    private int getBootRank(BootSize bootSize) {
        if (bootSize == null) return 2;
        switch (bootSize) {
            case S: return 1;
            case M: return 2;
            case L: return 3;
            default: return 2;
        }
    }

    private double computeTripTypeFit(CarType carType, TripType tripType) {
        if (tripType == null) return 80.0;

        switch (tripType) {
            case HILL_DRIVE:
                if (carType == CarType.SUV) return 100.0;
                if (carType == CarType.LUXURY) return 75.0;
                if (carType == CarType.SEDAN) return 60.0;
                return 40.0;

            case WEDDING:
                if (carType == CarType.LUXURY) return 100.0;
                if (carType == CarType.SEDAN) return 85.0;
                if (carType == CarType.SUV) return 75.0;
                return 35.0;

            case OFFICE_COMMUTE:
                if (carType == CarType.EV) return 100.0;
                if (carType == CarType.HATCHBACK) return 95.0;
                if (carType == CarType.SEDAN) return 80.0;
                return 50.0;

            case SOLO_WEEKEND:
                if (carType == CarType.HATCHBACK || carType == CarType.EV) return 95.0;
                if (carType == CarType.SEDAN) return 85.0;
                if (carType == CarType.LUXURY) return 80.0;
                return 70.0;

            case AIRPORT_TRANSFER:
                if (carType == CarType.SEDAN || carType == CarType.SUV) return 95.0;
                if (carType == CarType.LUXURY) return 90.0;
                return 65.0;

            case FAMILY_ROAD_TRIP:
            default:
                if (carType == CarType.SUV) return 100.0;
                if (carType == CarType.SEDAN) return 85.0;
                if (carType == CarType.LUXURY) return 75.0;
                return 50.0;
        }
    }

    private String generateReason(Car car, VibeRequest req, int seatDiff, int bootRank, int reqRank) {
        StringBuilder sb = new StringBuilder();
        if (car.getType() == CarType.SUV) {
            sb.append("Ideal high ground clearance and spacious cabin for ").append(req.getPassengers()).append(" people");
        } else if (car.getType() == CarType.EV) {
            sb.append("Zero-emission electric efficiency, ideal for smooth urban commute");
        } else if (car.getType() == CarType.LUXURY) {
            sb.append("Premium luxury styling with supreme plush ride quality");
        } else if (car.getType() == CarType.HATCHBACK) {
            sb.append("Nimble city handling with comfortable seats for ").append(req.getPassengers()).append(" passengers");
        } else {
            sb.append("Balanced touring sedan with generous legroom");
        }

        if (bootRank >= reqRank) {
            sb.append(" and ample ").append(car.getBootSize().name()).append("-size luggage boot.");
        } else {
            sb.append(" with great fuel economy.");
        }
        return sb.toString();
    }
}
