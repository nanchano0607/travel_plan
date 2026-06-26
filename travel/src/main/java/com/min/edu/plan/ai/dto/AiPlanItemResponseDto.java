package com.min.edu.plan.ai.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPlanItemResponseDto {

    private Integer dayNumber;
    private Integer sequence;
    private String placeName;
    private String placeId;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
