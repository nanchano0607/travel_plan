package com.min.edu.plan.ai.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPlanInsightResponseDto {

    private String currency;
    private String budgetComment;
    private List<String> assumptions;
    private List<AiPlanInsightItemResponseDto> items;
}
