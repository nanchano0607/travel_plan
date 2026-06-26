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
                - 시작일: %s
                - 종료일: %s
                - 여행 일수: %d일
                - 인원수: %d명
                - 예산: %d원

                [생성 규칙]
                - dayNumber는 1부터 %d까지만 사용하세요.
                - 각 dayNumber 안에서 sequence는 1부터 순서대로 배치하세요.
                - placeName은 실제 존재하는 장소명으로 작성하세요.
                - latitude와 longitude는 숫자로 작성하세요.
                - placeId는 아직 모르면 null로 작성하세요.
                """.formatted(
                requestDto.getRegionName(),
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                tripDays,
                requestDto.getHeadcount(),
                requestDto.getBudget(),
                tripDays
        );
    }
}
