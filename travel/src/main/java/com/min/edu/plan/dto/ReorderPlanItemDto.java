package com.min.edu.plan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReorderPlanItemDto {

    @NotNull(message = "일정 항목 ID는 필수입니다.")
    private Long planItemId;

    @NotNull(message = "일차는 필수입니다.")
    @Min(value = 1, message = "일차는 1 이상이어야 합니다.")
    private Integer dayNumber;

    @NotNull(message = "방문 순서는 필수입니다.")
    @Min(value = 1, message = "방문 순서는 1 이상이어야 합니다.")
    private Integer sequence;
}
