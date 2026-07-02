package com.min.edu.plan.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavePlanDto {

    private Long userId;

    @NotBlank(message = "여행 계획 제목은 필수입니다.")
    @Size(max = 255, message = "여행 계획 제목은 255자 이하로 입력해야 합니다.")
    private String title;

    @Size(max = 100, message = "지역 이름은 100자 이하로 입력해야 합니다.")
    private String regionName;

    @Size(max = 255, message = "지역 ID는 255자 이하로 입력해야 합니다.")
    private String regionId;

    @NotNull(message = "예산은 필수입니다.")
    @PositiveOrZero(message = "예산은 0 이상이어야 합니다.")
    private Integer budget;

    @NotNull(message = "인원은 필수입니다.")
    @Min(value = 1, message = "인원은 1명 이상이어야 합니다.")
    private Integer headcount;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate endDate;

    private List<@Valid SavePlanItemDto> planItems;
}
