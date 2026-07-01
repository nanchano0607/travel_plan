package com.min.edu.plan.dto;

import java.math.BigDecimal;

import com.min.edu.plan.entity.PlanItem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReusablePlanItemDto {
    private String placeName;
    private String placeId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String oneLineReview;
    private Integer estimatedCost;

    public static ReusablePlanItemDto from(PlanItem item) {
        return new ReusablePlanItemDto(
                item.getPlaceName(),
                item.getPlaceId(),
                item.getLatitude(),
                item.getLongitude(),
                item.getOneLineReview(),
                item.getEstimatedCost());
    }
}
