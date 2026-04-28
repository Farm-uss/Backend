package com.example.practice.service.chatbot;

import com.example.practice.common.config.OpenAiProperties;
import com.example.practice.dto.chatbot.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final WebClient openAiWebClient;
    private final OpenAiProperties openAiProperties;

    public ChatResponse ask(String message) {
        Map<String, Object> requestBody = Map.of(
                "model", openAiProperties.getModel(),
                "input", message
        );

        Map<String, Object> response = openAiWebClient.post()
                .uri("/v1/responses")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String answer = extractAnswer(response);

        return ChatResponse.builder()
                .answer(answer)
                .build();
    }

    private String extractAnswer(Map<String, Object> response) {
        if (response == null) {
            return null;
        }

        Object outputObj = response.get("output");
        if (!(outputObj instanceof List<?> outputList) || outputList.isEmpty()) {
            return null;
        }

        Object firstOutput = outputList.get(0);
        if (!(firstOutput instanceof Map<?, ?> outputMap)) {
            return null;
        }

        Object contentObj = outputMap.get("content");
        if (!(contentObj instanceof List<?> contentList) || contentList.isEmpty()) {
            return null;
        }

        Object firstContent = contentList.get(0);
        if (!(firstContent instanceof Map<?, ?> contentMap)) {
            return null;
        }

        Object textObj = contentMap.get("text");
        return textObj instanceof String ? (String) textObj : null;
    }
}
