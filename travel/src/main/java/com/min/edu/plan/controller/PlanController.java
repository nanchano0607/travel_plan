package com.min.edu.plan.controller;

import java.util.List;

import com.min.edu.common.response.ApiResponse;
import com.min.edu.plan.ai.dto.PlanRequestDto;
import com.min.edu.plan.ai.service.ChatService;
import com.min.edu.plan.dto.PlanItemResponseDto;
import com.min.edu.plan.dto.SavePlanDto;
import com.min.edu.plan.dto.SavePlanResponseDto;
import com.min.edu.plan.dto.UpdatePlanItemPlaceDto;
import com.min.edu.plan.service.PlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @PostMapping
    @Operation(summary = "여행 계획 저장", description = "AI 생성 초안 또는 사용자가 수정한 여행 계획을 저장합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "여행 계획 저장 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 오류")
    public ResponseEntity<ApiResponse<SavePlanResponseDto>> savePlan(@Valid @RequestBody SavePlanDto requestDto) {
        SavePlanResponseDto response = planService.savePlan(requestDto);
        return ResponseEntity.ok(ApiResponse.success("여행계획 저장에 성공하셨습니다", response));
    }

    @PostMapping("/ai")
    @Operation(summary = "AI 여행 계획 초안 생성", description = "여행 조건을 기반으로 AI 여행 일정 초안을 생성합니다. DB에는 저장하지 않습니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AI 여행 계획 초안 생성 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 오류 또는 AI 응답 파싱 실패")
    public ResponseEntity<ApiResponse<SavePlanDto>> createAiPlanDraft(@Valid @RequestBody PlanRequestDto requestDto) {
        SavePlanDto response = chatService.createPlanDraft(requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{planId}/items/{dayNumber}/{sequence}")
    @Operation(summary = "일정 항목 장소 수정", description = "Google Places에서 선택한 장소 정보로 특정 일정 항목을 수정합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일정 항목 장소 수정 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 오류")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "여행 계획 또는 일정 항목 없음")
    public ResponseEntity<ApiResponse<PlanItemResponseDto>> updatePlanItemPlace(
            @PathVariable Long planId,
            @PathVariable Integer dayNumber,
            @PathVariable Integer sequence,
            @Valid @RequestBody UpdatePlanItemPlaceDto requestDto
    ) {
        PlanItemResponseDto response = planService.updatePlanItemPlace(planId, dayNumber, sequence, requestDto);
        return ResponseEntity.ok(ApiResponse.success("일정 항목 수정에 성공하셨습니다", response));
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
