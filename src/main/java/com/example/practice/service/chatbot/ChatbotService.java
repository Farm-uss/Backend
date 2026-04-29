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
        requestBody.put("instructions", "사용자에게 한국어로 짧고 간단하게 한두 문장 이내로 답변해줘.");
        requestBody.put("max_output_tokens", 300);

        Map<String, Object> reasoning = new HashMap<>();
        reasoning.put("effort", "low");
        requestBody.put("reasoning", reasoning);

        Map<String, Object> response = openAiWebClient.post()
                .uri("https://api.openai.com/v1/responses")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        System.out.println("OPENAI RESPONSE = " + response);

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
            return null;
        }

        StringBuilder sb = new StringBuilder();

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
                Object text = contentMap.get("text");

                if ("output_text".equals(type) && text instanceof String textValue) {
                    if (!sb.isEmpty()) {
                        sb.append("\n");
                    }
                    sb.append(textValue);
                }
            }
        }

        return sb.isEmpty() ? null : sb.toString();
    }
}
