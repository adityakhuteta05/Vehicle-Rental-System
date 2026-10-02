package com.drivesense.repository;

import com.drivesense.entity.Booking;
import com.drivesense.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    List<Booking> findAllByOrderByCreatedAtDesc();

    List<Booking> findByCarOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<Booking> findByCarOwnerIdAndStatusOrderByCreatedAtDesc(Long ownerId, BookingStatus status);

    long countByCarOwnerId(Long ownerId);

    long countByCarOwnerIdAndStatus(Long ownerId, BookingStatus status);

    @Query("""
        SELECT COALESCE(SUM(b.ownerEarnings), 0) FROM Booking b
        WHERE b.car.owner.id = :ownerId
          AND b.status IN ('CONFIRMED', 'ACTIVE', 'COMPLETED')
    """)
    BigDecimal sumOwnerEarnings(@Param("ownerId") Long ownerId);

    @Query("""
        SELECT COALESCE(SUM(b.ownerEarnings), 0) FROM Booking b
        WHERE b.car.owner.id = :ownerId
          AND b.status IN ('CONFIRMED', 'ACTIVE', 'COMPLETED')
          AND b.createdAt >= :since
    """)
    BigDecimal sumOwnerEarningsSince(@Param("ownerId") Long ownerId, @Param("since") LocalDateTime since);

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.car.id = :carId
          AND b.status IN ('CONFIRMED','ACTIVE')
          AND b.startTime < :end AND b.endTime > :start
    """)
    long countOverlaps(
            @Param("carId") Long carId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT b FROM Booking b
        WHERE b.car.id = :carId
          AND b.status IN ('CONFIRMED','ACTIVE')
          AND b.endTime >= :fromTime
        ORDER BY b.startTime ASC
    """)
    List<Booking> findUpcomingOrActiveByCar(
            @Param("carId") Long carId,
            @Param("fromTime") LocalDateTime fromTime
    );

    long countByStatus(BookingStatus status);

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.status = 'ACTIVE'
    """)
    long countCarsOutNow();

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.status = 'ACTIVE'
          AND b.endTime BETWEEN :startOfDay AND :endOfDay
    """)
    long countDueToday(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
        SELECT COALESCE(SUM(b.totalAmount + b.extraCharges), 0) FROM Booking b
        WHERE b.status IN ('CONFIRMED', 'ACTIVE', 'COMPLETED')
          AND b.createdAt >= :since
    """)
    BigDecimal sumTotalRevenueSince(@Param("since") LocalDateTime since);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.status = 'ACTIVE'
          AND b.endTime BETWEEN :startOfDay AND :endOfDay
        ORDER BY b.endTime ASC
    """)
    List<Booking> findDueTodayBookings(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    List<Booking> findTop10ByStatusOrderByStartTimeAsc(BookingStatus status);
}
