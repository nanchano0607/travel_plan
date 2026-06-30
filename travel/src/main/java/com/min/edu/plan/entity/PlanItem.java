package com.min.edu.plan.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class PlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private String placeName;

    @Column(nullable = false)
    private Integer dayNumber;

    private Integer sequence;

    private String placeId;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public PlanItem(Plan plan, String placeName, Integer dayNumber, Integer sequence, 
                    String placeId, BigDecimal latitude, BigDecimal longitude) {
        this.plan = plan;
        this.placeName = placeName;
        this.dayNumber = dayNumber;
        this.sequence = sequence;
        this.placeId = placeId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updatePlace(String placeName, String placeId, BigDecimal latitude, BigDecimal longitude) {
        this.placeName = placeName;
        this.placeId = placeId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
