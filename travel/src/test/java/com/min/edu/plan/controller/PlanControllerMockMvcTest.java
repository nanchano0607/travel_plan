package com.min.edu.plan.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.min.edu.exception.GlobalExceptionHandler;
import com.min.edu.plan.ai.dto.AiPlanInsightItemResponseDto;
import com.min.edu.plan.ai.dto.AiPlanInsightResponseDto;
import com.min.edu.plan.ai.dto.RetryPlanRequestDto;
import com.min.edu.plan.ai.service.ChatService;
import com.min.edu.plan.dto.SavePlanDto;
import com.min.edu.plan.dto.SavePlanResponseDto;
import com.min.edu.plan.service.PlanService;
import com.min.edu.security.jwt.JwtTokenProvider;

@WebMvcTest(controllers = PlanController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PlanControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlanService planService;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("여행 계획 저장 요청 성공")
    void savePlan_success() throws Exception {
        SavePlanResponseDto response = new SavePlanResponseDto(
                1L,
                1L,
                "서울 2박 3일 여행",
                "서울",
                "seoul-region-id",
                300000,
                2,
                false,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12),
                null,
                Collections.emptyList()
        );

        given(planService.savePlan(any(SavePlanDto.class))).willReturn(response);

        mockMvc.perform(post("/api/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "title": "서울 2박 3일 여행",
                                  "regionName": "서울",
                                  "regionId": "seoul-region-id",
                                  "budget": 300000,
                                  "headcount": 2,
                                  "startDate": "2026-07-10",
                                  "endDate": "2026-07-12",
                                  "planItems": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("여행계획 저장에 성공하셨습니다"))
                .andExpect(jsonPath("$.data.planId").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.title").value("서울 2박 3일 여행"))
                .andExpect(jsonPath("$.data.regionName").value("서울"))
                .andExpect(jsonPath("$.data.budget").value(300000))
                .andExpect(jsonPath("$.data.headcount").value(2));
    }

    @Test
    @DisplayName("여행 계획 제목이 비어 있으면 저장 요청 실패")
    void savePlan_blankTitle_fail() throws Exception {
        mockMvc.perform(post("/api/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "title": "",
                                  "regionName": "서울",
                                  "regionId": "seoul-region-id",
                                  "budget": 300000,
                                  "headcount": 2,
                                  "startDate": "2026-07-10",
                                  "endDate": "2026-07-12",
                                  "planItems": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("여행 계획 제목은 필수입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(planService, never()).savePlan(any(SavePlanDto.class));
    }

    @Test
    @DisplayName("인원이 1명 미만이면 저장 요청 실패")
    void savePlan_invalidHeadcount_fail() throws Exception {
        mockMvc.perform(post("/api/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "title": "서울 2박 3일 여행",
                                  "regionName": "서울",
                                  "regionId": "seoul-region-id",
                                  "budget": 300000,
                                  "headcount": 0,
                                  "startDate": "2026-07-10",
                                  "endDate": "2026-07-12",
                                  "planItems": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("인원은 1명 이상이어야 합니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(planService, never()).savePlan(any(SavePlanDto.class));
    }

    @Test
    @DisplayName("일정 항목 일차가 1 미만이면 저장 요청 실패")
    void savePlan_invalidPlanItemDayNumber_fail() throws Exception {
        mockMvc.perform(post("/api/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "title": "서울 2박 3일 여행",
                                  "regionName": "서울",
                                  "regionId": "seoul-region-id",
                                  "budget": 300000,
                                  "headcount": 2,
                                  "startDate": "2026-07-10",
                                  "endDate": "2026-07-12",
                                  "planItems": [
                                    {
                                      "placeName": "경복궁",
                                      "dayNumber": 0,
                                      "sequence": 1,
                                      "placeId": "gyeongbokgung-place-id",
                                      "latitude": 37.579617,
                                      "longitude": 126.977041
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("일차는 1 이상이어야 합니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(planService, never()).savePlan(any(SavePlanDto.class));
    }

    @Test
    @DisplayName("장소 좌표가 허용 범위를 벗어나면 저장 요청 실패")
    void savePlan_invalidCoordinate_fail() throws Exception {
        mockMvc.perform(post("/api/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "title": "서울 2박 3일 여행",
                                  "regionName": "서울",
                                  "regionId": "seoul-region-id",
                                  "budget": 300000,
                                  "headcount": 2,
                                  "startDate": "2026-07-10",
                                  "endDate": "2026-07-12",
                                  "planItems": [
                                    {
                                      "placeName": "경복궁",
                                      "dayNumber": 1,
                                      "sequence": 1,
                                      "placeId": "gyeongbokgung-place-id",
                                      "latitude": 91.0,
                                      "longitude": 126.977041
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("위도는 90 이하여야 합니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(planService, never()).savePlan(any(SavePlanDto.class));
    }

    @Test
    @DisplayName("AI 여행 계획 초안 재생성 요청 성공")
    void retryAiPlanDraft_success() throws Exception {
        SavePlanDto response = new SavePlanDto(
                1L,
                "서울 여행",
                "서울",
                "seoul-region-id",
                0,
                2,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12),
                Collections.emptyList()
        );

        given(chatService.retryPlanDraft(any(RetryPlanRequestDto.class))).willReturn(response);

        mockMvc.perform(post("/api/plan/ai/retry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "condition": {
                                    "regionName": "서울",
                                    "regionId": "seoul-region-id",
                                    "latitude": 37.5665,
                                    "longitude": 126.9780,
                                    "headcount": 2,
                                    "startDate": "2026-07-10",
                                    "endDate": "2026-07-12"
                                  },
                                  "previousPlanItems": [
                                    {
                                      "placeName": "경복궁",
                                      "dayNumber": 1,
                                      "sequence": 1,
                                      "placeId": "gyeongbokgung-place-id",
                                      "latitude": 37.579617,
                                      "longitude": 126.977041
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.title").value("서울 여행"))
                .andExpect(jsonPath("$.data.regionName").value("서울"));

        verify(chatService).retryPlanDraft(any(RetryPlanRequestDto.class));
    }

    @Test
    @DisplayName("AI 여행 계획 비용/한줄평 분석 요청 성공")
    void createAiPlanInsight_success() throws Exception {
        AiPlanInsightItemResponseDto item = new AiPlanInsightItemResponseDto();
        item.setDayNumber(1);
        item.setSequence(1);
        item.setPlaceName("경복궁");
        item.setOneLineReview("고궁 산책과 사진 촬영에 좋아요.");
        item.setEstimatedCost(3000);

        AiPlanInsightResponseDto response = new AiPlanInsightResponseDto();
        response.setCurrency("KRW");
        response.setBudgetComment("입장료와 지역 이동비 중심의 예상 금액입니다.");
        response.setAssumptions(List.of("항공권과 숙박비는 제외했습니다."));
        response.setItems(List.of(item));

        given(chatService.createPlanInsight(any(SavePlanDto.class))).willReturn(response);

        mockMvc.perform(post("/api/plan/ai/insight")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "title": "서울 여행",
                                  "regionName": "서울",
                                  "regionId": "seoul-region-id",
                                  "budget": 300000,
                                  "headcount": 2,
                                  "startDate": "2026-07-10",
                                  "endDate": "2026-07-12",
                                  "planItems": [
                                    {
                                      "placeName": "경복궁",
                                      "dayNumber": 1,
                                      "sequence": 1,
                                      "placeId": "gyeongbokgung-place-id",
                                      "latitude": 37.579617,
                                      "longitude": 126.977041
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.currency").value("KRW"))
                .andExpect(jsonPath("$.data.items[0].dayNumber").value(1))
                .andExpect(jsonPath("$.data.items[0].sequence").value(1))
                .andExpect(jsonPath("$.data.items[0].placeName").value("경복궁"))
                .andExpect(jsonPath("$.data.items[0].oneLineReview").value("고궁 산책과 사진 촬영에 좋아요."))
                .andExpect(jsonPath("$.data.items[0].estimatedCost").value(3000));

        verify(chatService).createPlanInsight(any(SavePlanDto.class));
    }

    @Test
    @DisplayName("여행 계획 단건 조회 성공")
    void getPlan_success() throws Exception {
        SavePlanResponseDto response = new SavePlanResponseDto(
                1L,
                1L,
                "서울 2박 3일 여행",
                "서울",
                "seoul-region-id",
                300000,
                2,
                false,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12),
                null,
                Collections.emptyList()
        );

        given(planService.getPlan(1L)).willReturn(response);

        mockMvc.perform(get("/api/plan/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 성공했습니다."))
                .andExpect(jsonPath("$.data.planId").value(1))
                .andExpect(jsonPath("$.data.title").value("서울 2박 3일 여행"));
    }

    @Test
    @DisplayName("존재하지 않는 여행 계획 단건 조회 실패")
    void getPlan_notFound_fail() throws Exception {
        given(planService.getPlan(999L)).willThrow(new IllegalArgumentException("여행 계획을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/plan/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("PLAN_INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("여행 계획을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("사용자별 여행 계획 목록 조회 성공")
    void getPlansByUserId_success() throws Exception {
        SavePlanResponseDto response = new SavePlanResponseDto(
                1L,
                1L,
                "서울 2박 3일 여행",
                "서울",
                "seoul-region-id",
                300000,
                2,
                false,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 12),
                null,
                Collections.emptyList()
        );

        given(planService.getPlansByUserId(1L)).willReturn(List.of(response));

        mockMvc.perform(get("/api/plan")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("요청이 성공했습니다."))
                .andExpect(jsonPath("$.data[0].planId").value(1))
                .andExpect(jsonPath("$.data[0].title").value("서울 2박 3일 여행"));
    }

    @Test
    @DisplayName("여행 계획 삭제 성공")
    void deletePlan_success() throws Exception {
        mockMvc.perform(delete("/api/plan/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("여행계획 삭제에 성공하셨습니다"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(planService).deletePlan(1L);
    }

    @Test
    @DisplayName("존재하지 않는 여행 계획 삭제 실패")
    void deletePlan_notFound_fail() throws Exception {
        org.mockito.BDDMockito.willThrow(new IllegalArgumentException("여행 계획을 찾을 수 없습니다.")).given(planService).deletePlan(999L);

        mockMvc.perform(delete("/api/plan/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("PLAN_INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("여행 계획을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
