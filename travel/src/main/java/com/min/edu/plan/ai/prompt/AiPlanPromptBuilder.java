package com.min.edu.plan.ai.prompt;

import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.min.edu.plan.ai.dto.PlanRequestDto;

@Component
public class AiPlanPromptBuilder {

    public String build(PlanRequestDto requestDto) {
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
                tripDays
        );
    }
}
