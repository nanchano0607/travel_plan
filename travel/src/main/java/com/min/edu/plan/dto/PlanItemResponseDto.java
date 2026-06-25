package com.min.edu.plan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.min.edu.plan.entity.PlanItem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlanItemResponseDto {

    private Long planItemId;
    private String placeName;
    private Integer dayNumber;
    private Integer sequence;
    private String placeId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime createdAt;

    public static PlanItemResponseDto from(PlanItem planItem) {
        return new PlanItemResponseDto(
                planItem.getPlanItemId(),
                planItem.getPlaceName(),
                planItem.getDayNumber(),
                planItem.getSequence(),
                planItem.getPlaceId(),
                planItem.getLatitude(),
                planItem.getLongitude(),
                planItem.getCreatedAt()
        );
    }
}
