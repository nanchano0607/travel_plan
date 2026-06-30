package com.min.edu.plan.ai.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

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
import com.min.edu.plan.place.PlaceValidationService;
import com.min.edu.plan.place.ValidatedPlace;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Long TEMP_USER_ID = 1L;
    private static final int DEFAULT_AI_PLAN_BUDGET = 0;
    private static final int MAX_TRIP_DAYS = 7; // ai응답 텍스트를 고려하여 최대 7일이라는 제한을 둠.
    private static final Pattern PARENTHESIS_PATTERN = Pattern.compile("\\([^)]*\\)");

    private final AssistantAi assistantAi;
    private final ObjectMapper objectMapper;
    private final AiPlanPromptBuilder aiPlanPromptBuilder;
    private final PlaceValidationService placeValidationService;

    public SavePlanDto createPlanDraft(PlanRequestDto requestDto) {
        log.info("AI plan request started. regionName={}, regionId={}, startDate={}, endDate={}",
                requestDto.getRegionName(), requestDto.getRegionId(), requestDto.getStartDate(), requestDto.getEndDate());
        validatePlanRequest(requestDto);
        String prompt = aiPlanPromptBuilder.build(requestDto);
        List<AiPlanItemResponseDto> aiPlanItems = requestAiPlanItems(prompt);
        log.info("AI returned {} plan items.", aiPlanItems.size());
        validateAiPlanItems(requestDto, aiPlanItems);
        return createSavePlanDto(requestDto, aiPlanItems);
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
                .flatMap(Optional::stream)
                .toList();

        if (planItems.isEmpty()) {
            log.warn("All AI plan items failed Google Places validation. regionName={}", requestDto.getRegionName());
            throw new CustomException(ErrorCode.PLACE_VALIDATION_FAILED);
        }

        List<SavePlanItemDto> normalizedPlanItems = normalizeSequences(planItems);

        return new SavePlanDto(
                TEMP_USER_ID,
                requestDto.getRegionName() + " 여행",
                requestDto.getRegionName(),
                requestDto.getRegionId(),
                DEFAULT_AI_PLAN_BUDGET,
                requestDto.getHeadcount(),
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                normalizedPlanItems);
    }

    private Optional<SavePlanItemDto> createSavePlanItemDto(PlanRequestDto requestDto, AiPlanItemResponseDto aiPlanItem) {
        log.info("Correcting AI place with Google Places. placeName={}, aiLatitude={}, aiLongitude={}",
                aiPlanItem.getPlaceName(), aiPlanItem.getLatitude(), aiPlanItem.getLongitude());
        Optional<ValidatedPlace> validatedPlaceResult = placeValidationService.validateAndCorrect(requestDto, aiPlanItem);
        if (validatedPlaceResult.isEmpty()) {
            log.warn("Skipping AI place because Google place validation failed. placeName={}, dayNumber={}, sequence={}",
                    aiPlanItem.getPlaceName(), aiPlanItem.getDayNumber(), aiPlanItem.getSequence());
            return Optional.empty();
        }

        ValidatedPlace validatedPlace = validatedPlaceResult.get();
        log.info("Google place validation succeeded. aiPlaceName={}, googlePlaceName={}, placeId={}, latitude={}, longitude={}",
                aiPlanItem.getPlaceName(), validatedPlace.getPlaceName(), validatedPlace.getPlaceId(),
                validatedPlace.getLatitude(), validatedPlace.getLongitude());

        return Optional.of(new SavePlanItemDto(
                resolveDisplayPlaceName(aiPlanItem.getPlaceName(), validatedPlace.getPlaceName()),
                aiPlanItem.getDayNumber(),
                aiPlanItem.getSequence(),
                validatedPlace.getPlaceId(),
                validatedPlace.getLatitude(),
                validatedPlace.getLongitude()));
    }

    private List<SavePlanItemDto> normalizeSequences(List<SavePlanItemDto> planItems) {
        List<SavePlanItemDto> normalizedPlanItems = new ArrayList<>(planItems);
        normalizedPlanItems.sort(Comparator
                .comparing(SavePlanItemDto::getDayNumber)
                .thenComparing(SavePlanItemDto::getSequence));

        Integer currentDayNumber = null;
        int sequence = 0;
        for (SavePlanItemDto planItem : normalizedPlanItems) {
            if (!planItem.getDayNumber().equals(currentDayNumber)) {
                currentDayNumber = planItem.getDayNumber();
                sequence = 1;
            } else {
                sequence++;
            }

            if (!planItem.getSequence().equals(sequence)) {
                log.info("Renumbering plan item sequence. placeName={}, dayNumber={}, oldSequence={}, newSequence={}",
                        planItem.getPlaceName(), planItem.getDayNumber(), planItem.getSequence(), sequence);
                planItem.setSequence(sequence);
            }
        }

        return normalizedPlanItems;
    }

    private String resolveDisplayPlaceName(String aiPlaceName, String googlePlaceName) {
        String cleanedAiPlaceName = cleanAiPlaceName(aiPlaceName);
        if (shouldKeepAiPlaceName(cleanedAiPlaceName, googlePlaceName)) {
            log.info("Using AI place name for display because Google display name is ambiguous. aiPlaceName={}, googlePlaceName={}",
                    cleanedAiPlaceName, googlePlaceName);
            return cleanedAiPlaceName;
        }

        return googlePlaceName;
    }

    private boolean shouldKeepAiPlaceName(String aiPlaceName, String googlePlaceName) {
        String normalizedAiName = normalizeDisplayName(aiPlaceName);
        String normalizedGoogleName = normalizeDisplayName(googlePlaceName);

        return !normalizedAiName.isBlank()
                && !normalizedGoogleName.isBlank()
                && normalizedAiName.length() > normalizedGoogleName.length()
                && normalizedAiName.contains(normalizedGoogleName)
                && normalizedGoogleName.length() <= 2;
    }

    private String cleanAiPlaceName(String aiPlaceName) {
        if (aiPlaceName == null) {
            return "";
        }

        return PARENTHESIS_PATTERN.matcher(aiPlaceName).replaceAll("").trim();
    }

    private String normalizeDisplayName(String placeName) {
        if (placeName == null) {
            return "";
        }

        return placeName
                .toLowerCase()
                .replaceAll("[\\s·・\\-_'\"()\\[\\],.]", "");
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
