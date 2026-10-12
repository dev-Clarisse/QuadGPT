package com.QuadGPT.backend.llm;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class LlmClientService {

    private final RestClient zaiRestClient;
    private final String model;

    public LlmClientService(
            RestClient zaiRestClient,
            @Value("${zai.model}") String model
    ) {
        this.zaiRestClient = zaiRestClient;
        this.model = model;
    }

    public String chat(String userMessage) {
        var request = new ZaiDtos.ChatRequest(
                model,
                List.of(new ZaiDtos.ChatMessage("user", userMessage))
        );

        ZaiDtos.ChatResponse response = zaiRestClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(ZaiDtos.ChatResponse.class);

        if (response == null || response.choices().isEmpty()) {
            throw new IllegalStateException("Réponse vide du LLM");
        }

        return response.choices().get(0).message().content();
    }
}