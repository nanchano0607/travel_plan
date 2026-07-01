package com.min.edu.plan.ai.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import com.min.edu.plan.ai.entity.AiRequestType;
import com.min.edu.plan.ai.entity.AiRequestUsage;
import com.min.edu.plan.ai.repository.AiRequestUsageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiRequestLimitService {

    private static final int DAILY_REQUEST_LIMIT = 100;
    private static final int COOLDOWN_SECONDS = 30;

    private final AiRequestUsageRepository aiRequestUsageRepository;

    @Transactional
    public void checkAndIncrease(Long userId, AiRequestType requestType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        long totalRequestCount = aiRequestUsageRepository.sumRequestCountByUserIdAndUsageDate(userId, today);
        if (totalRequestCount >= DAILY_REQUEST_LIMIT) {
            throw new CustomException(ErrorCode.AI_REQUEST_LIMIT_EXCEEDED);
        }

        LocalDateTime lastRequestedAt = aiRequestUsageRepository
                .findLatestRequestedAtByUserIdAndUsageDate(userId, today)
                .orElse(null);
        if (lastRequestedAt != null && lastRequestedAt.plusSeconds(COOLDOWN_SECONDS).isAfter(now)) {
            throw new CustomException(ErrorCode.AI_REQUEST_TOO_FREQUENT);
        }

        AiRequestUsage usage = aiRequestUsageRepository
                .findByUserIdAndRequestTypeAndUsageDate(userId, requestType, today)
                .orElseGet(() -> new AiRequestUsage(userId, requestType, today));

        usage.increaseRequestCount(now);
        aiRequestUsageRepository.save(usage);
    }
}
