package com.min.edu.plan.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReorderPlanItemsDto {

    @NotEmpty(message = "순서를 변경할 일정 항목은 필수입니다.")
    private List<@Valid ReorderPlanItemDto> items;
}
