package com.example.practice.controller.chatbot;

import com.example.practice.common.response.ApiResponse;
import com.example.practice.dto.chatbot.ChatRequest;
import com.example.practice.dto.chatbot.ChatResponse;
import com.example.practice.service.chatbot.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping
    public ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ApiResponse.success(chatbotService.ask(request.getMessage()));
    }
}
