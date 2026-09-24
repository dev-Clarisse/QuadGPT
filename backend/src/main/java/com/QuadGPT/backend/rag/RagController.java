package com.QuadGPT.backend.rag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final IngestionService ingestionService;
    private final RagService ragService;

    public RagController(IngestionService ingestionService, RagService ragService) {
        this.ingestionService = ingestionService;
        this.ragService = ragService;
    }

    public record IngestRequest(String name, String text) {}
    public record AskRequest(String question) {}

    @PostMapping("/ingest")
    public String ingest(@RequestBody IngestRequest request) {
        ingestionService.ingest(request.name(), request.text());
        return "Document ingéré : " + request.name();
    }

    @PostMapping("/ask")
    public String ask(@RequestBody AskRequest request) {
        return ragService.ask(request.question());
    }
}