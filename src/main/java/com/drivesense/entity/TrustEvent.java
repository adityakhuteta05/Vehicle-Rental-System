package com.drivesense.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trust_events", indexes = {
    @Index(name = "idx_trust_event_user", columnList = "user_id")
})
public class TrustEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String reason;

    @Column(nullable = false)
    private int delta;

    @Column(name = "resulting_score", nullable = false)
    private int resultingScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public TrustEvent() {}

    public TrustEvent(User user, String reason, int delta, int resultingScore) {
        this.user = user;
        this.reason = reason;
        this.delta = delta;
        this.resultingScore = resultingScore;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getDelta() {
        return delta;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }

    public int getResultingScore() {
        return resultingScore;
    }

    public void setResultingScore(int resultingScore) {
        this.resultingScore = resultingScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
