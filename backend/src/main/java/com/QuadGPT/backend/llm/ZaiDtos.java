package com.QuadGPT.backend.llm;

import java.util.List;

public class ZaiDtos {

    public record ChatMessage(String role, String content) {}

    public record ChatRequest(
            String model,
            List<ChatMessage> messages
    ) {}

    public record ChatChoice(ChatMessage message) {}

    public record ChatResponse(List<ChatChoice> choices) {}
}