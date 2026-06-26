package com.min.edu.plan.ai.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.min.edu.plan.ai.llm.AssistantAi;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

@Configuration
public class AiConfig {
    @Bean("basicAi")
public AssistantAi basicAi(@Qualifier("geminiModel") ChatModel model, ChatMemory chatMemory){
    
    String systemPrompt = """
            당신은 10년 차 베테랑 '맞춤형 여행 플래너'입니다.
            사용자가 제시하는 여행 지역, 기간, 예산, 인원수를 바탕으로 최적의 여행 일정을 짜는 것이 당신의 임무입니다.
            
            [제약 조건]
            1. 동선 최적화: 이동 시간을 최소화할 수 있도록 지리적으로 가까운 장소들을 묶어서 순서(sequence)를 배치하세요.
            2. 현실성: 실제로 존재하는 유명 관광지, 식당, 숙소(Google Places에서 검색 가능한 곳)만 추천하세요.
            3. 데이터 정확성: 각 장소의 대략적인 위도(latitude)와 경도(longitude) 값을 반드시 포함하세요.
            
            [출력 형식]
            사용자에게 인사말이나 부연 설명을 절대 하지 마세요.
            당신의 응답은 백엔드 서버에서 파싱해야 하므로, 반드시 아래 JSON 배열 구조로만 대답해야 한다.
            응답은 반드시 JSON 배열만 반환한다.
            Markdown 코드블록, ```json, 설명 문장, 인사말을 절대 포함하지 않는다.
            
            [
              {
                "dayNumber": 1,
                "sequence": 1,
                "placeName": "도쿄 타워",
                "latitude": 35.658580,
                "longitude": 139.745432
              },
              {
                "dayNumber": 1,
                "sequence": 2,
                "placeName": "롯폰기 힐즈",
                "latitude": 35.660464,
                "longitude": 139.729249
              }
            ]
            """;

    return AiServices.builder(AssistantAi.class)
                    .chatModel(model)
                    .chatMemory(chatMemory)
                    .systemMessage(systemPrompt) // 구체적인 프롬프트 주입
                    .build();
}

}
