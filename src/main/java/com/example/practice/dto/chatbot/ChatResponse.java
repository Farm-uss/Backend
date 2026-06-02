package com.example.practice.dto.chatbot;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatResponse {
    private String answer;
}
