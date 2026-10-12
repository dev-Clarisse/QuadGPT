package com.QuadGPT.backend.llm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class ZaiDtos {

    public record ChatMessage(String role, String content) {}

    public record ChatRequest(
            String model,
            List<ChatMessage> messages
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatChoice(ChatMessage message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatResponse(List<ChatChoice> choices) {}
}