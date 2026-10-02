package com.drivesense.repository;

import com.drivesense.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByBookingId(Long bookingId);
    List<Review> findByCarIdOrderByCreatedAtDesc(Long carId);
    boolean existsByBookingId(Long bookingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.car.id = :carId")
    Double getAverageRatingForCar(@Param("carId") Long carId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.car.id = :carId")
    long countByCarId(@Param("carId") Long carId);
}
