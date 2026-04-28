package com.example.practice.service.chatbot;

import com.example.practice.common.config.OpenAiProperties;
import com.example.practice.dto.chatbot.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final WebClient openAiWebClient;
    private final OpenAiProperties openAiProperties;

    public ChatResponse ask(String message) {
        if (message == null || message.isBlank()) {
            return ChatResponse.builder()
                    .answer("메시지를 입력해주세요.")
                    .build();
        }

        String model = openAiProperties.getModel();
        if (model == null || model.isBlank()) {
            model = "gpt-5-mini";
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("input", message);

        Map<String, Object> response = openAiWebClient.post()
                .uri("https://api.openai.com/v1/responses")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String answer = extractOutputText(response);

        return ChatResponse.builder()
                .answer(answer != null ? answer : "응답을 생성하지 못했습니다.")
                .build();
    }

    private String extractOutputText(Map<String, Object> response) {
        if (response == null) {
            return null;
        }

        Object output = response.get("output");
        if (!(output instanceof List<?> outputList)) {
            Object fallback = response.get("output_text");
            return fallback instanceof String ? (String) fallback : null;
        }

        for (Object outputItem : outputList) {
            if (!(outputItem instanceof Map<?, ?> outputMap)) {
                continue;
            }

            Object content = outputMap.get("content");
            if (!(content instanceof List<?> contentList)) {
                continue;
            }

            for (Object contentItem : contentList) {
                if (!(contentItem instanceof Map<?, ?> contentMap)) {
                    continue;
                }

                Object type = contentMap.get("type");
                if ("output_text".equals(type)) {
                    Object text = contentMap.get("text");
                    if (text instanceof String textValue) {
                        return textValue;
                    }
                }
            }
        }

        return null;
    }
}
