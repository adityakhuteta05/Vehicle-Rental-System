package com.drivesense.service;

import com.drivesense.entity.Booking;
import com.drivesense.entity.Car;
import com.drivesense.entity.Review;
import com.drivesense.entity.User;
import com.drivesense.enums.BookingStatus;
import com.drivesense.event.ReviewSubmittedEvent;
import com.drivesense.exception.ResourceNotFoundException;
import com.drivesense.repository.BookingRepository;
import com.drivesense.repository.CarRepository;
import com.drivesense.repository.ReviewRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReviewService(ReviewRepository reviewRepository,
                         BookingRepository bookingRepository,
                         CarRepository carRepository,
                         ApplicationEventPublisher eventPublisher) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Review addReview(Long bookingId, User user, int rating, String comment) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only review your own trips.");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("Reviews can only be submitted after a trip is COMPLETED.");
        }

        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new IllegalStateException("A review has already been submitted for this trip.");
        }

        Car car = booking.getCar();
        Review review = new Review(booking, user, car, rating, comment);
        Review saved = reviewRepository.save(review);

        // Update car average rating (RV-2)
        Double avg = reviewRepository.getAverageRatingForCar(car.getId());
        if (avg != null) {
            car.setAvgRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
            carRepository.save(car);
        }

        eventPublisher.publishEvent(new ReviewSubmittedEvent(saved));
        return saved;
    }

    public List<Review> getReviewsForCar(Long carId) {
        return reviewRepository.findByCarIdOrderByCreatedAtDesc(carId);
    }
}
