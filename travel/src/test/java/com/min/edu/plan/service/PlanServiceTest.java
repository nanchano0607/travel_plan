package com.min.edu.plan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.min.edu.plan.dto.SavePlanDto;
import com.min.edu.plan.dto.SavePlanItemDto;
import com.min.edu.plan.dto.SavePlanResponseDto;
import com.min.edu.plan.entity.Plan;
import com.min.edu.plan.repository.PlanRepository;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanService planService;

    @Test
    @DisplayName("여행 계획 저장 성공")
    void savePlan_success() {
        SavePlanDto request = new SavePlanDto(
                1L,
                "서울 2박 3일 여행",
                "서울",
                "seoul-region-id",
                300000,
                2,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12),
                List.of(new SavePlanItemDto(
                        "경복궁",
                        1,
                        1,
                        "gyeongbokgung-place-id",
                        new BigDecimal("37.579617"),
                        new BigDecimal("126.977041")
                ))
        );

        given(planRepository.save(any(Plan.class))).willAnswer(invocation -> invocation.getArgument(0));

        SavePlanResponseDto response = planService.savePlan(request);

        assertThat(response.getTitle()).isEqualTo("서울 2박 3일 여행");
        assertThat(response.getPlanItems()).hasSize(1);
        assertThat(response.getPlanItems().get(0).getPlaceName()).isEqualTo("경복궁");
    }

    @Test
    @DisplayName("종료일이 시작일보다 빠르면 저장 실패")
    void savePlan_invalidDateRange_fail() {
        SavePlanDto request = new SavePlanDto(
                1L,
                "서울 여행",
                "서울",
                "seoul-region-id",
                300000,
                2,
                LocalDate.of(2026, 7, 12),
                LocalDate.of(2026, 7, 10),
                List.of()
        );

        assertThatThrownBy(() -> planService.savePlan(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료일은 시작일보다 빠를 수 없습니다.");
    }

    @Test
    @DisplayName("일정 일차가 여행 기간을 초과하면 저장 실패")
    void savePlan_planItemDayOutOfRange_fail() {
        SavePlanDto request = new SavePlanDto(
                1L,
                "서울 여행",
                "서울",
                "seoul-region-id",
                300000,
                2,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12),
                List.of(new SavePlanItemDto(
                        "경복궁",
                        4,
                        1,
                        "gyeongbokgung-place-id",
                        new BigDecimal("37.579617"),
                        new BigDecimal("126.977041")
                ))
        );

        assertThatThrownBy(() -> planService.savePlan(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("일정 일차는 여행 기간을 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("여행 계획 단건 조회 성공")
    void getPlan_success() {
        Plan plan = new Plan(
                1L,
                "서울 2박 3일 여행",
                "서울",
                "seoul-region-id",
                300000,
                2,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12)
        );

        given(planRepository.findById(1L)).willReturn(Optional.of(plan));

        SavePlanResponseDto response = planService.getPlan(1L);

        assertThat(response.getTitle()).isEqualTo("서울 2박 3일 여행");
        assertThat(response.getRegionName()).isEqualTo("서울");
    }

    @Test
    @DisplayName("존재하지 않는 여행 계획 단건 조회 실패")
    void getPlan_notFound_fail() {
        given(planRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> planService.getPlan(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("여행 계획을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("사용자별 여행 계획 목록 조회 성공")
    void getPlansByUserId_success() {
        Plan plan = new Plan(
                1L,
                "서울 2박 3일 여행",
                "서울",
                "seoul-region-id",
                300000,
                2,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12)
        );

        given(planRepository.findByUserId(1L)).willReturn(List.of(plan));

        List<SavePlanResponseDto> response = planService.getPlansByUserId(1L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getTitle()).isEqualTo("서울 2박 3일 여행");
    }

    @Test
    @DisplayName("여행 계획 삭제 성공")
    void deletePlan_success() {
        Plan plan = new Plan(
                1L,
                "서울 2박 3일 여행",
                "서울",
                "seoul-region-id",
                300000,
                2,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12)
        );

        given(planRepository.findById(1L)).willReturn(Optional.of(plan));

        planService.deletePlan(1L);

        verify(planRepository).delete(plan);
    }

    @Test
    @DisplayName("존재하지 않는 여행 계획 삭제 실패")
    void deletePlan_notFound_fail() {
        given(planRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> planService.deletePlan(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("여행 계획을 찾을 수 없습니다.");
    }
}
