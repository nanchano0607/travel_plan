package com.min.edu.plan.ai.service;

import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.min.edu.plan.ai.dto.AiPlanItemResponseDto;
import com.min.edu.plan.ai.dto.PlanRequestDto;
import com.min.edu.plan.ai.llm.AssistantAi;
import com.min.edu.plan.ai.prompt.AiPlanPromptBuilder;
import com.min.edu.plan.dto.SavePlanDto;
import com.min.edu.plan.dto.SavePlanItemDto;
import com.min.edu.plan.dto.SavePlanResponseDto;
import com.min.edu.plan.service.PlanService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Long TEMP_USER_ID = 1L;
    private static final int MAX_TRIP_DAYS = 7; //ai응답 텍스트를 고려하여 최대 7일이라는 제한을 둠.

    private final AssistantAi assistantAi;
    private final ObjectMapper objectMapper;
    private final AiPlanPromptBuilder aiPlanPromptBuilder;
    private final PlanService planService;

    public SavePlanResponseDto createPlan(PlanRequestDto requestDto) {
        validatePlanRequest(requestDto);
        String prompt = aiPlanPromptBuilder.build(requestDto);
        List<AiPlanItemResponseDto> aiPlanItems = requestAiPlanItems(prompt);
        SavePlanDto savePlanDto = createSavePlanDto(requestDto, aiPlanItems);

        return planService.savePlan(savePlanDto);
    }

    private void validatePlanRequest(PlanRequestDto requestDto) {
        if (requestDto.getEndDate().isBefore(requestDto.getStartDate())) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }

        // AI 응답 길이와 장소 품질을 안정적으로 유지하기 위해 한 번에 생성할 수 있는 일수를 제한한다.
        long tripDays = ChronoUnit.DAYS.between(requestDto.getStartDate(), requestDto.getEndDate()) + 1;
        if (tripDays > MAX_TRIP_DAYS) {
            throw new IllegalArgumentException("AI 여행 일정은 최대 7일까지 생성할 수 있습니다.");
        }
    }

    private List<AiPlanItemResponseDto> requestAiPlanItems(String prompt) {
        String response = assistantAi.chat(prompt);
        String jsonResponse = removeMarkdownCodeBlock(response);
        try {
            return objectMapper.readValue(jsonResponse, new TypeReference<List<AiPlanItemResponseDto>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("AI 응답 형식이 올바르지 않습니다.");
        }
    }

    private SavePlanDto createSavePlanDto(PlanRequestDto requestDto, List<AiPlanItemResponseDto> aiPlanItems) {
        List<SavePlanItemDto> planItems = aiPlanItems.stream()
                .map(this::createSavePlanItemDto)
                .toList();

        return new SavePlanDto(
                TEMP_USER_ID,
                requestDto.getRegionName() + " 여행",
                requestDto.getRegionName(),
                null,
                requestDto.getBudget(),
                requestDto.getHeadcount(),
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                planItems
        );
    }

    private SavePlanItemDto createSavePlanItemDto(AiPlanItemResponseDto aiPlanItem) {
        return new SavePlanItemDto(
                aiPlanItem.getPlaceName(),
                aiPlanItem.getDayNumber(),
                aiPlanItem.getSequence(),
                aiPlanItem.getPlaceId(),
                aiPlanItem.getLatitude(),
                aiPlanItem.getLongitude()
        );
    }

    private String removeMarkdownCodeBlock(String response) {
        if (response == null) {
            return "";
        }

        // Gemini 설정 변경이나 일시적 출력 흔들림으로 코드블록이 섞여도 파싱이 깨지지 않도록 제거한다.
        return response
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
}
