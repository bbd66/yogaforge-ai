package com.myfitpet.yoga;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_metrics")
public class UserMetrics {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_calories", nullable = false)
    private Long totalCalories;

    @Column(name = "total_sessions", nullable = false)
    private Integer totalSessions;

    @Column(name = "total_yoga_seconds", nullable = false)
    private Long totalYogaSeconds;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (totalCalories == null) totalCalories = 0L;
        if (totalSessions == null) totalSessions = 0;
        if (totalYogaSeconds == null) totalYogaSeconds = 0L;
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTotalCalories() { return totalCalories; }
    public void setTotalCalories(Long totalCalories) { this.totalCalories = totalCalories; }
    public Integer getTotalSessions() { return totalSessions; }
    public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }
    public Long getTotalYogaSeconds() { return totalYogaSeconds; }
    public void setTotalYogaSeconds(Long totalYogaSeconds) { this.totalYogaSeconds = totalYogaSeconds; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}