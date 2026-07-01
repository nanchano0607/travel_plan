package com.min.edu.plan.ai.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "ai_request_usage",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ai_request_usage_user_type_date",
                        columnNames = {"user_id", "request_type", "usage_date"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRequestUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 50)
    private AiRequestType requestType;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "request_count", nullable = false)
    private Integer requestCount = 0;

    @Column(name = "last_requested_at")
    private LocalDateTime lastRequestedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AiRequestUsage(Long userId, AiRequestType requestType, LocalDate usageDate) {
        this.userId = userId;
        this.requestType = requestType;
        this.usageDate = usageDate;
        this.requestCount = 0;
    }

    public void increaseRequestCount(LocalDateTime requestedAt) {
        this.requestCount++;
        this.lastRequestedAt = requestedAt;
    }
}
