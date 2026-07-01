package com.min.edu.plan.ai.prompt;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.min.edu.plan.dto.SavePlanDto;
import com.min.edu.plan.dto.SavePlanItemDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AiPlanInsightPromptBuilder {

    private final ObjectMapper objectMapper;

    public String build(SavePlanDto planDraft) {
        return """
                아래 여행 초안을 기준으로 예상 총 비용과 장소별 한줄평을 작성하세요.

                [중요 규칙]
                - planItems에 없는 장소를 새로 만들지 마세요.
                - 응답 items는 입력 planItems와 같은 dayNumber, sequence를 사용하세요.
                - 입력의 budget은 사용자가 입력한 희망 예산입니다. 응답에는 estimatedBudget을 만들지 마세요.
                - 항공권과 숙박비는 제외하고, 식비/카페/입장료/체험비/일정 내 지역 이동비 중심으로 계산하세요.
                - estimatedCost는 해당 장소에서 발생할 수 있는 인원수 전체 기준의 예상 비용입니다.
                - 전체 예상 비용은 서버와 프론트에서 estimatedCost 합산으로 계산합니다.

                [여행 초안 JSON]
                %s
                """.formatted(toJson(createPromptPayload(planDraft)));
    }

    private Map<String, Object> createPromptPayload(SavePlanDto planDraft) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("travelCondition", createTravelCondition(planDraft));
        payload.put("planItems", createPlanItems(planDraft));
        return payload;
    }

    private Map<String, Object> createTravelCondition(SavePlanDto planDraft) {
        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("title", planDraft.getTitle());
        condition.put("regionName", planDraft.getRegionName());
        condition.put("regionId", planDraft.getRegionId());
        condition.put("userBudget", planDraft.getBudget());
        condition.put("headcount", planDraft.getHeadcount());
        condition.put("startDate", planDraft.getStartDate() == null ? null : planDraft.getStartDate().toString());
        condition.put("endDate", planDraft.getEndDate() == null ? null : planDraft.getEndDate().toString());
        condition.put("tripDays", calculateTripDays(planDraft));
        return condition;
    }

    private List<Map<String, Object>> createPlanItems(SavePlanDto planDraft) {
        if (planDraft.getPlanItems() == null) {
            return List.of();
        }

        return planDraft.getPlanItems().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(SavePlanItemDto::getDayNumber, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SavePlanItemDto::getSequence, Comparator.nullsLast(Integer::compareTo)))
                .map(this::createPlanItem)
                .toList();
    }

    private Map<String, Object> createPlanItem(SavePlanItemDto planItem) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("dayNumber", planItem.getDayNumber());
        item.put("sequence", planItem.getSequence());
        item.put("placeName", planItem.getPlaceName());
        item.put("placeId", planItem.getPlaceId());
        item.put("latitude", planItem.getLatitude());
        item.put("longitude", planItem.getLongitude());
        return item;
    }

    private long calculateTripDays(SavePlanDto planDraft) {
        if (planDraft.getStartDate() == null || planDraft.getEndDate() == null) {
            return 0;
        }

        return ChronoUnit.DAYS.between(planDraft.getStartDate(), planDraft.getEndDate()) + 1;
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize AI plan insight prompt payload.", e);
        }
    }
}
