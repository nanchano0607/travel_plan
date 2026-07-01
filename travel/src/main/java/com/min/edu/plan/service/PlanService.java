package com.min.edu.plan.service;

import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import com.min.edu.plan.dto.PlanItemResponseDto;
import com.min.edu.plan.dto.ReorderPlanItemDto;
import com.min.edu.plan.dto.ReorderPlanItemsDto;
import com.min.edu.plan.dto.SavePlanResponseDto;
import com.min.edu.plan.dto.SavePlanDto;
import com.min.edu.plan.dto.SavePlanItemDto;
import com.min.edu.plan.dto.UpdatePlanItemPlaceDto;
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
                        itemDto.getLongitude(),
                        itemDto.getOneLineReview(),
                        itemDto.getEstimatedCost()
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
                .orElseThrow(() -> new CustomException(ErrorCode.PLAN_NOT_FOUND));

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
                .orElseThrow(() -> new CustomException(ErrorCode.PLAN_NOT_FOUND));

        planRepository.delete(plan);
    }

    public PlanItemResponseDto updatePlanItemPlace(
            Long planId,
            Integer dayNumber,
            Integer sequence,
            UpdatePlanItemPlaceDto updateDto
    ) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAN_NOT_FOUND));

        PlanItem planItem = plan.getPlanItems()
                .stream()
                .filter(item -> dayNumber.equals(item.getDayNumber()) && sequence.equals(item.getSequence()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.PLAN_ITEM_NOT_FOUND));

        planItem.updatePlace(
                updateDto.getRegionName(),
                updateDto.getRegionId(),
                updateDto.getLatitude(),
                updateDto.getLongitude()
        );

        return PlanItemResponseDto.from(planItem);
    }

    public SavePlanResponseDto addPlanItem(Long planId, SavePlanItemDto itemDto) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAN_NOT_FOUND));

        validatePlanItemWithinDateRange(plan, itemDto.getDayNumber());

        int targetSequence = itemDto.getSequence() == null
                ? getNextSequence(plan, itemDto.getDayNumber())
                : itemDto.getSequence();

        shiftSequencesForInsert(plan, itemDto.getDayNumber(), targetSequence);

        PlanItem planItem = new PlanItem(
                plan,
                itemDto.getPlaceName(),
                itemDto.getDayNumber(),
                targetSequence,
                itemDto.getPlaceId(),
                itemDto.getLatitude(),
                itemDto.getLongitude(),
                itemDto.getOneLineReview(),
                itemDto.getEstimatedCost()
        );

        plan.getPlanItems().add(planItem);
        planRepository.flush();
        return SavePlanResponseDto.from(plan);
    }

    public SavePlanResponseDto reorderPlanItems(Long planId, ReorderPlanItemsDto requestDto) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAN_NOT_FOUND));

        List<PlanItem> planItems = plan.getPlanItems();
        if (planItems.size() != requestDto.getItems().size()) {
            throw new CustomException(ErrorCode.PLAN_ITEM_ORDER_INVALID);
        }

        Map<Long, PlanItem> planItemMap = planItems.stream()
                .collect(Collectors.toMap(PlanItem::getPlanItemId, Function.identity()));

        Set<Long> requestedIds = new HashSet<>();
        Set<String> requestedSchedules = new HashSet<>();
        for (ReorderPlanItemDto itemDto : requestDto.getItems()) {
            PlanItem planItem = planItemMap.get(itemDto.getPlanItemId());
            if (planItem == null || !requestedIds.add(itemDto.getPlanItemId())) {
                throw new CustomException(ErrorCode.PLAN_ITEM_ORDER_INVALID);
            }

            validatePlanItemWithinDateRange(plan, itemDto.getDayNumber());
            if (!requestedSchedules.add(itemDto.getDayNumber() + "-" + itemDto.getSequence())) {
                throw new CustomException(ErrorCode.PLAN_ITEM_ORDER_INVALID);
            }
        }

        for (ReorderPlanItemDto itemDto : requestDto.getItems()) {
            planItemMap.get(itemDto.getPlanItemId())
                    .updateSchedule(itemDto.getDayNumber(), itemDto.getSequence());
        }

        return SavePlanResponseDto.from(plan);
    }

    private void validateDateRange(SavePlanDto savePlanDto) {
        if (savePlanDto.getEndDate().isBefore(savePlanDto.getStartDate())) {
            throw new CustomException(ErrorCode.PLAN_INVALID_DATE_RANGE);
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
                throw new CustomException(ErrorCode.PLAN_ITEM_DAY_OUT_OF_RANGE);
            }
        }
    }

    private void validatePlanItemWithinDateRange(Plan plan, Integer dayNumber) {
        long tripDays = ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate()) + 1;
        if (dayNumber > tripDays) {
            throw new CustomException(ErrorCode.PLAN_ITEM_DAY_OUT_OF_RANGE);
        }
    }

    private int getNextSequence(Plan plan, Integer dayNumber) {
        return plan.getPlanItems()
                .stream()
                .filter(item -> dayNumber.equals(item.getDayNumber()))
                .map(PlanItem::getSequence)
                .filter(sequence -> sequence != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private void shiftSequencesForInsert(Plan plan, Integer dayNumber, Integer targetSequence) {
        plan.getPlanItems()
                .stream()
                .filter(item -> dayNumber.equals(item.getDayNumber()))
                .filter(item -> item.getSequence() != null && item.getSequence() >= targetSequence)
                .forEach(item -> item.updateSchedule(dayNumber, item.getSequence() + 1));
    }
}
