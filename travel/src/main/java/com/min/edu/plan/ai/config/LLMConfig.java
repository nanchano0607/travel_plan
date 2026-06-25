package com.min.edu.plan.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

@Configuration
public class LLMConfig {

    @Value("${gemini.api.key}")
    private String geminiApiKey;
    @Value("${gemini.model}")
    private String geminiModel;

    @Bean("geminiModel")
    public ChatModel geminiChatModel(){
        return GoogleAiGeminiChatModel.builder()
                    .apiKey(geminiApiKey)
                    .modelName(geminiModel)
                    .build();
    }
}
