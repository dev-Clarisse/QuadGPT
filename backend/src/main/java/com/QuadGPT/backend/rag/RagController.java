package com.QuadGPT.backend.rag;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final IngestionService ingestionService;
    private final RagService ragService;
    private final PdfExtractionService pdfExtractionService;

    public RagController(
            IngestionService ingestionService,
            RagService ragService,
            PdfExtractionService pdfExtractionService
    ) {
        this.ingestionService = ingestionService;
        this.ragService = ragService;
        this.pdfExtractionService = pdfExtractionService;
    }

    public record IngestRequest(String name, String text, String department) {}
    public record AskRequest(String question, String department) {}

    @PostMapping("/ingest")
    public String ingest(@RequestBody IngestRequest request) {
        ingestionService.ingest(request.name(), request.text(), request.department());
        return "Document ingéré : " + request.name();
    }

    @PostMapping("/ingest-file")
    public String ingestFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "department", required = false) String department
    ) {
        String text = pdfExtractionService.extractText(file);
        ingestionService.ingest(file.getOriginalFilename(), text, department);
        return "Fichier ingéré : " + file.getOriginalFilename();
    }

    @PostMapping("/ask")
    public String ask(@RequestBody AskRequest request) {
        return ragService.ask(request.question(), request.department());
    }
}