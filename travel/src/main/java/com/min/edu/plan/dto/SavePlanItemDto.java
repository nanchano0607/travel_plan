package com.min.edu.plan.dto;

import java.math.BigDecimal;

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
public class SavePlanItemDto {

    @NotBlank(message = "장소 이름은 필수입니다.")
    @Size(max = 255, message = "장소 이름은 255자 이하로 입력해야 합니다.")
    private String placeName;

    @NotNull(message = "일차는 필수입니다.")
    @Min(value = 1, message = "일차는 1 이상이어야 합니다.")
    private Integer dayNumber;

    @Min(value = 1, message = "방문 순서는 1 이상이어야 합니다.")
    private Integer sequence;

    @Size(max = 255, message = "장소 ID는 255자 이하로 입력해야 합니다.")
    private String placeId;

    @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
    private BigDecimal longitude;

}
