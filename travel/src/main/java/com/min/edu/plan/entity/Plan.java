package com.min.edu.plan.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    private String regionName;

    private String regionId;

    private Integer budget;

    private Integer headcount;

    private boolean isPublic = false;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
    private List<PlanItem> planItems = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Plan(Long userId, String title, String regionName, String regionId, Integer budget, Integer headcount,
            LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.title = title;
        this.regionName = regionName;
        this.regionId = regionId;
        this.budget = budget;
        this.headcount = headcount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

}
