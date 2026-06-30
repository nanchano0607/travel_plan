package com.min.edu.plan.ai.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import com.min.edu.plan.ai.dto.AiPlanItemResponseDto;
import com.min.edu.plan.ai.dto.PlanRequestDto;
import com.min.edu.plan.ai.llm.AssistantAi;
import com.min.edu.plan.ai.prompt.AiPlanPromptBuilder;
import com.min.edu.plan.dto.SavePlanDto;
import com.min.edu.plan.dto.SavePlanItemDto;
import com.min.edu.plan.dto.SavePlanResponseDto;
import com.min.edu.plan.place.PlaceValidationService;
import com.min.edu.plan.place.ValidatedPlace;
import com.min.edu.plan.service.PlanService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Long TEMP_USER_ID = 1L;
    private static final int MAX_TRIP_DAYS = 7; // ai응답 텍스트를 고려하여 최대 7일이라는 제한을 둠.

    private final AssistantAi assistantAi;
    private final ObjectMapper objectMapper;
    private final AiPlanPromptBuilder aiPlanPromptBuilder;
    private final PlanService planService;
    private final PlaceValidationService placeValidationService;

    public SavePlanResponseDto createPlan(PlanRequestDto requestDto) {
        log.info("AI plan request started. regionName={}, regionId={}, startDate={}, endDate={}",
                requestDto.getRegionName(), requestDto.getRegionId(), requestDto.getStartDate(), requestDto.getEndDate());
        validatePlanRequest(requestDto);
        String prompt = aiPlanPromptBuilder.build(requestDto);
        List<AiPlanItemResponseDto> aiPlanItems = requestAiPlanItems(prompt);
        log.info("AI returned {} plan items.", aiPlanItems.size());
        validateAiPlanItems(requestDto, aiPlanItems);
        SavePlanDto savePlanDto = createSavePlanDto(requestDto, aiPlanItems);

        return planService.savePlan(savePlanDto);
    }

    private void validatePlanRequest(PlanRequestDto requestDto) {
        if (requestDto.getEndDate().isBefore(requestDto.getStartDate())) {
            throw new CustomException(ErrorCode.PLAN_INVALID_DATE_RANGE);
        }

        // AI 응답 길이와 장소 품질을 안정적으로 유지하기 위해 한 번에 생성할 수 있는 일수를 제한한다.
        long tripDays = ChronoUnit.DAYS.between(requestDto.getStartDate(), requestDto.getEndDate()) + 1;
        if (tripDays > MAX_TRIP_DAYS) {
            throw new CustomException(ErrorCode.AI_TRIP_DAYS_EXCEEDED);
        }
    }

    private List<AiPlanItemResponseDto> requestAiPlanItems(String prompt) {
        String response;
        try {
            response = assistantAi.chat(prompt);
        } catch (RuntimeException e) {
            log.warn("AI call failed.", e);
            throw new CustomException(ErrorCode.AI_CALL_FAILED);
        }

        String jsonResponse = removeMarkdownCodeBlock(response);
        try {
            return objectMapper.readValue(jsonResponse, new TypeReference<List<AiPlanItemResponseDto>>() {
            });
        } catch (JsonProcessingException e) {
            log.warn("AI response parse failed. response={}", jsonResponse, e);
            throw new CustomException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    private void validateAiPlanItems(PlanRequestDto requestDto, List<AiPlanItemResponseDto> aiPlanItems) {
        if (aiPlanItems == null || aiPlanItems.isEmpty()) {
            throw new CustomException(ErrorCode.AI_RESPONSE_SCHEMA_INVALID);
        }

        long tripDays = ChronoUnit.DAYS.between(requestDto.getStartDate(), requestDto.getEndDate()) + 1;

        for (AiPlanItemResponseDto item : aiPlanItems) {
            log.info("Validating AI item schema. dayNumber={}, sequence={}, placeName={}, latitude={}, longitude={}",
                    item.getDayNumber(), item.getSequence(), item.getPlaceName(), item.getLatitude(), item.getLongitude());

            if (item.getDayNumber() == null
                    || item.getSequence() == null
                    || item.getPlaceName() == null
                    || item.getPlaceName().isBlank()
                    || item.getLatitude() == null
                    || item.getLongitude() == null) {
                throw new CustomException(ErrorCode.AI_RESPONSE_SCHEMA_INVALID);
            }

            if (item.getDayNumber() < 1 || item.getDayNumber() > tripDays) {
                throw new CustomException(ErrorCode.AI_RESPONSE_SCHEMA_INVALID);
            }

            if (item.getSequence() < 1) {
                throw new CustomException(ErrorCode.AI_RESPONSE_SCHEMA_INVALID);
            }

            if (!isBetween(item.getLatitude(), "-90.0", "90.0")
                    || !isBetween(item.getLongitude(), "-180.0", "180.0")) {
                throw new CustomException(ErrorCode.AI_RESPONSE_SCHEMA_INVALID);
            }
        }
    }

    private boolean isBetween(BigDecimal value, String min, String max) {
        return value.compareTo(new BigDecimal(min)) >= 0
                && value.compareTo(new BigDecimal(max)) <= 0;
    }

    private SavePlanDto createSavePlanDto(PlanRequestDto requestDto, List<AiPlanItemResponseDto> aiPlanItems) {
        List<SavePlanItemDto> planItems = aiPlanItems.stream()
                .map(aiPlanItem -> createSavePlanItemDto(requestDto, aiPlanItem))
                .toList();

        return new SavePlanDto(
                TEMP_USER_ID,
                requestDto.getRegionName() + " 여행",
                requestDto.getRegionName(),
                requestDto.getRegionId(),
                requestDto.getBudget(),
                requestDto.getHeadcount(),
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                planItems);
    }

    private SavePlanItemDto createSavePlanItemDto(PlanRequestDto requestDto, AiPlanItemResponseDto aiPlanItem) {
        log.info("Correcting AI place with Google Places. placeName={}, aiLatitude={}, aiLongitude={}",
                aiPlanItem.getPlaceName(), aiPlanItem.getLatitude(), aiPlanItem.getLongitude());
        ValidatedPlace validatedPlace = placeValidationService.validateAndCorrect(requestDto, aiPlanItem);
        log.info("Google place validation succeeded. aiPlaceName={}, googlePlaceName={}, placeId={}, latitude={}, longitude={}",
                aiPlanItem.getPlaceName(), validatedPlace.getPlaceName(), validatedPlace.getPlaceId(),
                validatedPlace.getLatitude(), validatedPlace.getLongitude());

        return new SavePlanItemDto(
                validatedPlace.getPlaceName(),
                aiPlanItem.getDayNumber(),
                aiPlanItem.getSequence(),
                validatedPlace.getPlaceId(),
                validatedPlace.getLatitude(),
                validatedPlace.getLongitude());
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
