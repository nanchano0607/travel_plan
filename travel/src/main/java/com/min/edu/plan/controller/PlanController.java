package com.min.edu.plan.controller;

import java.util.List;

import com.min.edu.common.response.ApiResponse;
import com.min.edu.plan.ai.dto.PlanRequestDto;
import com.min.edu.plan.ai.service.ChatService;
import com.min.edu.plan.dto.SavePlanResponseDto;
import com.min.edu.plan.service.PlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan")
@Tag(name = "Plan", description = "여행 계획 생성 / 조회 / 삭제 API")
public class PlanController {

    private final PlanService planService;
    private final ChatService chatService;

    @PostMapping("/ai")
    @Operation(summary = "AI 여행 계획 생성", description = "여행 조건을 기반으로 AI 여행 일정을 생성하고 저장합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AI 여행 계획 생성 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 오류 또는 AI 응답 파싱 실패")
    public ResponseEntity<ApiResponse<SavePlanResponseDto>> createAiPlan(@Valid @RequestBody PlanRequestDto requestDto) {
        SavePlanResponseDto response = chatService.createPlan(requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{planId}")
    @Operation(summary = "여행 계획 단건 조회", description = "여행 계획 ID로 저장된 여행 계획 상세 정보를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "여행 계획 조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "존재하지 않는 여행 계획")
    public ResponseEntity<ApiResponse<SavePlanResponseDto>> getPlan(@PathVariable Long planId) {
        SavePlanResponseDto response = planService.getPlan(planId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "사용자별 여행 계획 목록 조회", description = "사용자 ID로 저장된 여행 계획 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "여행 계획 목록 조회 성공")
    public ResponseEntity<ApiResponse<List<SavePlanResponseDto>>> getPlansByUserId(@RequestParam Long userId) {
        List<SavePlanResponseDto> response = planService.getPlansByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{planId}")
    @Operation(summary = "여행 계획 삭제", description = "여행 계획 ID로 저장된 여행 계획을 삭제합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "여행 계획 삭제 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "존재하지 않는 여행 계획")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable Long planId) {
        planService.deletePlan(planId);
        ApiResponse<Void> response = ApiResponse.success("여행계획 삭제에 성공하셨습니다", null);
        return ResponseEntity.ok(response);
    }
}
