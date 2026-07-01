package com.min.edu.plan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.min.edu.plan.entity.PlanItem;

public interface PlanItemRepository extends JpaRepository<PlanItem, Long> {
    @Query("""
                select pi
                from PlanItem pi
                join pi.plan p
                where p.regionName = :regionName
                  and pi.placeId is not null
                order by pi.createdAt desc
            """)
    List<PlanItem> findReusableItemsByRegionName(String regionName);
}
