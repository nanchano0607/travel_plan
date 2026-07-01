package com.min.edu.plan.ai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class PlanRequestDto {

    @NotBlank(message = "지역 이름은 필수입니다.")
    @Size(max = 100, message = "지역 이름은 100자 이하로 입력해야 합니다.")
    private String regionName;

    @NotBlank(message = "지역 ID는 필수입니다.")
    @Size(max = 255, message = "지역 ID는 255자 이하로 입력해야 합니다.")
    private String regionId;

    @NotNull(message = "지역 위도는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "지역 위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "지역 위도는 90 이하여야 합니다.")
    private BigDecimal latitude;

    @NotNull(message = "지역 경도는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "지역 경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "지역 경도는 180 이하여야 합니다.")
    private BigDecimal longitude;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate endDate;

    @NotNull(message = "인원은 필수입니다.")
    @Min(value = 1, message = "인원은 1명 이상이어야 합니다.")
    private Integer headcount;
}
