package com.min.edu.plan.ai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPlanInsightItemResponseDto {

    private Integer dayNumber;
    private Integer sequence;
    private String placeName;
    private String oneLineReview;
    private Integer estimatedCost;
}
