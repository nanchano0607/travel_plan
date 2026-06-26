package com.min.edu.plan.service;

import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.min.edu.plan.dto.SavePlanResponseDto;
import com.min.edu.plan.dto.SavePlanDto;
import com.min.edu.plan.dto.SavePlanItemDto;
import com.min.edu.plan.entity.Plan;
import com.min.edu.plan.entity.PlanItem;
import com.min.edu.plan.repository.PlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {
    private final PlanRepository planRepository;

    public SavePlanResponseDto savePlan(SavePlanDto savePlanDto) {
        validateDateRange(savePlanDto);
        validatePlanItemsWithinDateRange(savePlanDto);

        Plan plan = new Plan(
                savePlanDto.getUserId(),
                savePlanDto.getTitle(),
                savePlanDto.getRegionName(),
                savePlanDto.getRegionId(),
                savePlanDto.getBudget(),
                savePlanDto.getHeadcount(),
                savePlanDto.getStartDate(),
                savePlanDto.getEndDate()
        );

        if (savePlanDto.getPlanItems() != null) {
            for (SavePlanItemDto itemDto : savePlanDto.getPlanItems()) {
                PlanItem planItem = new PlanItem(
                        plan,
                        itemDto.getPlaceName(),
                        itemDto.getDayNumber(),
                        itemDto.getSequence(),
                        itemDto.getPlaceId(),
                        itemDto.getLatitude(),
                        itemDto.getLongitude()
                );

                plan.getPlanItems().add(planItem);
            }
        }

        Plan savedPlan = planRepository.save(plan);
        return SavePlanResponseDto.from(savedPlan);
    }

    @Transactional(readOnly = true)
    public SavePlanResponseDto getPlan(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다."));

        return SavePlanResponseDto.from(plan);
    }

    @Transactional(readOnly = true)
    public List<SavePlanResponseDto> getPlansByUserId(Long userId) {
        return planRepository.findByUserId(userId)
                .stream()
                .map(SavePlanResponseDto::from)
                .toList();
    }

    public void deletePlan(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다."));

        planRepository.delete(plan);
    }

    private void validateDateRange(SavePlanDto savePlanDto) {
        if (savePlanDto.getEndDate().isBefore(savePlanDto.getStartDate())) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }
    }

    private void validatePlanItemsWithinDateRange(SavePlanDto savePlanDto) {
        if (savePlanDto.getPlanItems() == null) {
            return;
        }

        // 종료일도 여행 기간에 포함되므로 날짜 차이에 1일을 더해 실제 여행 일수를 계산한다.
        long tripDays = ChronoUnit.DAYS.between(savePlanDto.getStartDate(), savePlanDto.getEndDate()) + 1;

        for (SavePlanItemDto itemDto : savePlanDto.getPlanItems()) {
            if (itemDto.getDayNumber() > tripDays) {
                throw new IllegalArgumentException("일정 일차는 여행 기간을 초과할 수 없습니다.");
            }
        }
    }
}
