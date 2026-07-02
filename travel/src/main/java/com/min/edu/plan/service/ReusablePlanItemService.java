package com.min.edu.plan.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.min.edu.plan.ai.dto.PlanRequestDto;
import com.min.edu.plan.dto.ReusablePlanItemDto;
import com.min.edu.plan.entity.PlanItem;
import com.min.edu.plan.repository.PlanItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReusablePlanItemService {
    private static final int MAX_CANDIDATE_COUNT = 15;

    private final PlanItemRepository planItemRepository;

    @Transactional(readOnly = true)
    public List<ReusablePlanItemDto> findCandidates(PlanRequestDto requestDto) {
        List<PlanItem> uniqueItems = new ArrayList<>(planItemRepository.findReusableItemsByRegionName(requestDto.getRegionName())
                .stream()
                .filter(item -> item.getPlaceId() != null && !item.getPlaceId().isBlank())
                .filter(item -> item.getLatitude() != null && item.getLongitude() != null)
                .collect(Collectors.toMap(
                        item -> item.getPlaceId(),
                        item -> item,
                        (first, ignored) -> first,
                        () -> new LinkedHashMap<>()))
                .values());

        Collections.shuffle(uniqueItems);

        return uniqueItems
                .stream()
                .limit(MAX_CANDIDATE_COUNT)
                .map(item -> ReusablePlanItemDto.from(item))
                .toList();
    }
}
