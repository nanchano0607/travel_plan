package com.min.edu.plan.ai.prompt;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.min.edu.plan.ai.dto.PlanRequestDto;
import com.min.edu.plan.dto.ReusablePlanItemDto;
import com.min.edu.plan.dto.SavePlanItemDto;

@Component
public class AiPlanPromptBuilder {

    public String build(PlanRequestDto requestDto) {
        return buildBasePrompt(requestDto);
    }

    public String buildRetry(PlanRequestDto requestDto, List<SavePlanItemDto> previousPlanItems) {
        return buildBasePrompt(requestDto) + """

                [이전 생성 결과]
                %s

                [재시도 규칙]
                - 위 이전 생성 결과와 같은 장소명은 가능하면 제외하세요.
                - 불가피하게 같은 대표 장소를 포함해야 한다면 dayNumber 또는 sequence를 다르게 구성하세요.
                - 같은 지역의 대표 관광지만 반복하지 말고, 식당/카페/전망/체험 장소를 새롭게 섞으세요.
                - dayNumber별 이동 동선도 이전과 다른 구역 또는 다른 순서가 되도록 재구성하세요.
                - 전체 일정이 이전 생성 결과와 뚜렷하게 달라야 합니다.
                - 장소는 여전히 실제 존재하고 Google Maps에서 검색 가능한 곳만 사용하세요.
                """.formatted(formatPreviousPlanItems(previousPlanItems));
    }

    public String build(PlanRequestDto requestDto, List<ReusablePlanItemDto> reusablePlanItems) {
        return buildBasePrompt(requestDto) + buildReusablePlanItemsPrompt(reusablePlanItems);
    }

    private String buildBasePrompt(PlanRequestDto requestDto) {
        long tripDays = ChronoUnit.DAYS.between(requestDto.getStartDate(), requestDto.getEndDate()) + 1;

        return """
                아래 여행 조건에 맞춰 여행 일정을 생성하세요.

                [여행 조건]
                - 지역: %s
                - Google 지역 ID: %s
                - 지역 중심 좌표: latitude %s, longitude %s
                - 시작일: %s
                - 종료일: %s
                - 여행 일수: %d일
                - 인원수: %d명

                [생성 규칙]
                - 추천 장소는 지역 중심 좌표 주변의 실제 장소로 구성하세요.
                - 이동 동선이 자연스럽도록 지리적으로 가까운 장소끼리 같은 dayNumber에 묶으세요.
                - dayNumber는 1부터 %d까지만 사용하세요.
                - 각 dayNumber 안에서 sequence는 1부터 순서대로 배치하세요.
                - placeName은 실제 존재하는 장소명으로 작성하세요.
                - latitude와 longitude는 가능한 실제 장소 좌표에 가깝게 숫자로 작성하세요.
                - placeId는 절대 추측하지 말고 항상 null로 작성하세요.
                """.formatted(
                requestDto.getRegionName(),
                requestDto.getRegionId(),
                requestDto.getLatitude(),
                requestDto.getLongitude(),
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                tripDays,
                requestDto.getHeadcount(),
                tripDays);
    }

    private String formatPreviousPlanItems(List<SavePlanItemDto> previousPlanItems) {
        if (previousPlanItems == null || previousPlanItems.isEmpty()) {
            return "- 없음";
        }

        String formattedItems = previousPlanItems.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(SavePlanItemDto::getDayNumber, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SavePlanItemDto::getSequence, Comparator.nullsLast(Integer::compareTo)))
                .map(item -> "- day %s sequence %s: %s (latitude %s, longitude %s)".formatted(
                        item.getDayNumber(),
                        item.getSequence(),
                        item.getPlaceName(),
                        item.getLatitude(),
                        item.getLongitude()))
                .collect(Collectors.joining("\n"));

        return formattedItems.isBlank() ? "- 없음" : formattedItems;
    }

    private String buildReusablePlanItemsPrompt(List<ReusablePlanItemDto> reusablePlanItems) {
        if (reusablePlanItems == null || reusablePlanItems.isEmpty()) {
            return "";
        }

        return """

                [기존 검증 장소 후보]
                아래 장소들은 이전 여행 계획에서 사용되었고 Google Places 검증을 통과한 장소입니다.
                일정 품질이 좋아진다면 일부 활용하세요.
                단, 전체 일정을 아래 후보로만 채우지 말고 새로운 장소도 섞으세요.
                카페/식당/관광지가 한쪽으로 치우치지 않게 구성하세요.
                후보의 placeId는 참고용입니다. 최종 응답 JSON의 placeId는 기존 규칙대로 null로 작성하세요.

                %s
                """.formatted(formatReusablePlanItems(reusablePlanItems));
    }

    private String formatReusablePlanItems(List<ReusablePlanItemDto> reusablePlanItems) {
        String formattedItems = reusablePlanItems.stream()
                .filter(Objects::nonNull)
                .map(item -> "- %s (placeId %s, latitude %s, longitude %s, review %s, estimatedCost %s)".formatted(
                        item.getPlaceName(),
                        item.getPlaceId(),
                        item.getLatitude(),
                        item.getLongitude(),
                        item.getOneLineReview(),
                        item.getEstimatedCost()))
                .collect(Collectors.joining("\n"));

        return formattedItems.isBlank() ? "- 없음" : formattedItems;
    }
}
