package com.min.edu.plan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.min.edu.plan.entity.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long>{
    Plan save(Plan plan);

    List<Plan> findByUserId(Long userId);
}
