package com.drivesense.repository;

import com.drivesense.entity.Car;
import com.drivesense.enums.CarStatus;
import com.drivesense.enums.CarType;
import com.drivesense.enums.FuelType;
import com.drivesense.enums.Transmission;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Car c WHERE c.id = :id")
    Optional<Car> findByIdWithLock(@Param("id") Long id);

    Optional<Car> findByRegNumber(String regNumber);

    List<Car> findByStatus(CarStatus status);

    List<Car> findTop6ByStatusOrderByAvgRatingDesc(CarStatus status);

    List<Car> findByOwnerId(Long ownerId);

    Optional<Car> findByIdAndOwnerId(Long id, Long ownerId);

    long countByOwnerId(Long ownerId);

    long countByOwnerIdAndStatus(Long ownerId, CarStatus status);

    @Query("""
        SELECT c FROM Car c
        WHERE (:status IS NULL OR c.status = :status)
          AND (:type IS NULL OR c.type = :type)
          AND (:fuelType IS NULL OR c.fuelType = :fuelType)
          AND (:transmission IS NULL OR c.transmission = :transmission)
          AND (:minSeats IS NULL OR c.seats >= :minSeats)
          AND (:maxPrice IS NULL OR c.pricePerDay <= :maxPrice)
          AND (:query IS NULL OR LOWER(c.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.model) LIKE LOWER(CONCAT('%', :query, '%')))
    """)
    Page<Car> searchCars(
            @Param("status") CarStatus status,
            @Param("type") CarType type,
            @Param("fuelType") FuelType fuelType,
            @Param("transmission") Transmission transmission,
            @Param("minSeats") Integer minSeats,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("query") String query,
            Pageable pageable
    );
}
