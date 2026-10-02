package com.drivesense.repository;

import com.drivesense.entity.TrustEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrustEventRepository extends JpaRepository<TrustEvent, Long> {
    List<TrustEvent> findByUserIdOrderByCreatedAtDesc(Long userId);
}
