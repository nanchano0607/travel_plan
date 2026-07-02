package com.min.edu.plan.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.min.edu.plan.entity.Plan;
import com.min.edu.plan.entity.PlanItem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SavePlanResponseDto {

    private Long planId;
    private Long userId;
    private String title;
    private String regionName;
    private String regionId;
    private Integer budget;
    private Integer headcount;
    private boolean isPublic;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private List<PlanItemResponseDto> planItems;

    public static SavePlanResponseDto from(Plan plan) {
        List<PlanItemResponseDto> planItems = plan.getPlanItems() == null
                ? Collections.emptyList()
                : plan.getPlanItems().stream()
                        .sorted(Comparator
                                .comparing(
                                        (PlanItem item) -> item.getDayNumber(),
                                        Comparator.nullsLast((Integer first, Integer second) -> Integer.compare(first, second)))
                                .thenComparing(
                                        (PlanItem item) -> item.getSequence(),
                                        Comparator.nullsLast((Integer first, Integer second) -> Integer.compare(first, second))))
                        .map(item -> PlanItemResponseDto.from(item))
                        .toList();

        return new SavePlanResponseDto(
                plan.getPlanId(),
                plan.getUserId(),
                plan.getTitle(),
                plan.getRegionName(),
                plan.getRegionId(),
                plan.getBudget(),
                plan.getHeadcount(),
                plan.isPublic(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getCreatedAt(),
                planItems
        );
    }
}
