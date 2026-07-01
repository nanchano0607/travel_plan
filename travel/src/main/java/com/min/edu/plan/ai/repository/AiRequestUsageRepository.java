package com.min.edu.plan.ai.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.min.edu.plan.ai.entity.AiRequestType;
import com.min.edu.plan.ai.entity.AiRequestUsage;

public interface AiRequestUsageRepository extends JpaRepository<AiRequestUsage, Long> {

    Optional<AiRequestUsage> findByUserIdAndRequestTypeAndUsageDate(
            Long userId,
            AiRequestType requestType,
            LocalDate usageDate
    );

    @Query("""
            select coalesce(sum(u.requestCount), 0)
            from AiRequestUsage u
            where u.userId = :userId
              and u.usageDate = :usageDate
            """)
    long sumRequestCountByUserIdAndUsageDate(
            @Param("userId") Long userId,
            @Param("usageDate") LocalDate usageDate
    );

    @Query("""
            select max(u.lastRequestedAt)
            from AiRequestUsage u
            where u.userId = :userId
              and u.usageDate = :usageDate
            """)
    Optional<LocalDateTime> findLatestRequestedAtByUserIdAndUsageDate(
            @Param("userId") Long userId,
            @Param("usageDate") LocalDate usageDate
    );
}
