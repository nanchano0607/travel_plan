package com.min.edu.plan.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.min.edu.common.response.ApiResponse;
import com.min.edu.plan.dto.SavePlanDto;
import com.min.edu.plan.dto.SavePlanResponseDto;
import com.min.edu.plan.service.PlanService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan")
public class PlanController {
    private final PlanService planService;

    @PostMapping
    public ResponseEntity<ApiResponse<SavePlanResponseDto>> savePlan(@Valid @RequestBody SavePlanDto savePlanDto) {
        SavePlanResponseDto response = planService.savePlan(savePlanDto);
        return ResponseEntity.ok(ApiResponse.success("여행계획 저장에 성공하셨습니다", response));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<SavePlanResponseDto>> getPlan(@PathVariable Long planId) {
        SavePlanResponseDto response = planService.getPlan(planId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavePlanResponseDto>>> getPlansByUserId(@RequestParam Long userId) {
        List<SavePlanResponseDto> response = planService.getPlansByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable Long planId) {
        planService.deletePlan(planId);
        ApiResponse<Void> response = ApiResponse.success("여행계획 삭제에 성공하셨습니다", null);
        return ResponseEntity.ok(response);
    }
}
