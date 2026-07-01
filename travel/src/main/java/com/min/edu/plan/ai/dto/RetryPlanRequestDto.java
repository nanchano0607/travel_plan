package com.min.edu.plan.ai.dto;

import java.util.List;

import com.min.edu.plan.dto.SavePlanItemDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetryPlanRequestDto {

    @Valid
    @NotNull(message = "여행 조건은 필수입니다.")
    private PlanRequestDto condition;

    @Valid
    @NotNull(message = "이전 여행 일정은 필수입니다.")
    @Size(min = 1, message = "이전 여행 일정은 1개 이상이어야 합니다.")
    private List<SavePlanItemDto> previousPlanItems;
}
