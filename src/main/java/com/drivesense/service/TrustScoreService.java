package com.drivesense.service;

import com.drivesense.entity.TrustEvent;
import com.drivesense.entity.User;
import com.drivesense.event.BookingCancelledEvent;
import com.drivesense.event.BookingCompletedEvent;
import com.drivesense.event.DamageReportedEvent;
import com.drivesense.event.LateReturnEvent;
import com.drivesense.event.ReviewSubmittedEvent;
import com.drivesense.repository.TrustEventRepository;
import com.drivesense.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrustScoreService {

    private static final Logger log = LoggerFactory.getLogger(TrustScoreService.class);

    private final UserRepository userRepository;
    private final TrustEventRepository trustEventRepository;

    public TrustScoreService(UserRepository userRepository, TrustEventRepository trustEventRepository) {
        this.userRepository = userRepository;
        this.trustEventRepository = trustEventRepository;
    }

    @Transactional
    public void adjustScore(User user, int delta, String reason) {
        if (user == null) return;
        int oldScore = user.getTrustScore();
        int newScore = Math.max(0, Math.min(100, oldScore + delta));
        user.setTrustScore(newScore);
        userRepository.save(user);

        TrustEvent event = new TrustEvent(user, reason, delta, newScore);
        trustEventRepository.save(event);

        log.info("Trust score updated for user {}: old={}, delta={}, new={}, reason='{}'",
                user.getEmail(), oldScore, delta, newScore, reason);
    }

    public List<TrustEvent> getAuditTrail(Long userId) {
        return trustEventRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Observer / Spring Event Listeners

    @EventListener
    @Transactional
    public void handleBookingCompleted(BookingCompletedEvent event) {
        if (event.isOnTime() && event.isNoDamage()) {
            adjustScore(event.getBooking().getUser(), +5, "Completed booking returned on time and zero damage (+5)");
        }
    }

    @EventListener
    @Transactional
    public void handleDamageReported(DamageReportedEvent event) {
        int count = event.getNewDamageZones() != null ? event.getNewDamageZones().size() : 1;
        adjustScore(event.getBooking().getUser(), -10, "New exterior/interior damage reported across " + count + " zone(s) (-10)");
    }

    @EventListener
    @Transactional
    public void handleLateReturn(LateReturnEvent event) {
        adjustScore(event.getBooking().getUser(), -5, "Late return penalty (-5)");
    }

    @EventListener
    @Transactional
    public void handleBookingCancelled(BookingCancelledEvent event) {
        if (event.isWithin24Hours()) {
            adjustScore(event.getBooking().getUser(), -3, "Late cancellation within 24h of pickup (-3)");
        }
    }

    @EventListener
    @Transactional
    public void handleReviewSubmitted(ReviewSubmittedEvent event) {
        if (event.getReview().getRating() == 5) {
            adjustScore(event.getReview().getUser(), +1, "Left a verified 5-star trip review (+1)");
        }
    }
}
