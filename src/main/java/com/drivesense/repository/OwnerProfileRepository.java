package com.drivesense.repository;

import com.drivesense.entity.OwnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerProfileRepository extends JpaRepository<OwnerProfile, Long> {
    Optional<OwnerProfile> findByUserId(Long userId);
    Optional<OwnerProfile> findByUserEmail(String email);
    boolean existsByUserId(Long userId);
}
