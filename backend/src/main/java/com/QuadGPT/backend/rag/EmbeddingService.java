package com.QuadGPT.backend.rag;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EmbeddingService {

    private final RestClient embeddingRestClient;
    private final String embeddingModel;

    public EmbeddingService(
            @Value("${embedding.base-url}") String baseUrl,
            @Value("${embedding.model}") String embeddingModel
    ) {
        this.embeddingRestClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.embeddingModel = embeddingModel;
    }

    public float[] embed(String text) {
        var request = new EmbeddingRequest(embeddingModel, text);

        EmbeddingResponse response = embeddingRestClient.post()
                .uri("/api/embed")
                .body(request)
                .retrieve()
                .body(EmbeddingResponse.class);

        if (response == null || response.embeddings() == null || response.embeddings().isEmpty()) {
            throw new IllegalStateException("Réponse d'embedding vide");
        }

        List<Double> values = response.embeddings().get(0);
        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i).floatValue();
        }
        return result;
    }

    private record EmbeddingRequest(String model, String input) {}
    private record EmbeddingResponse(List<List<Double>> embeddings) {}
}