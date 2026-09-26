package com.QuadGPT.backend.rag;

import java.util.Set;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.QuadGPT.backend.department.Department;

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

    public record IngestRequest(
            String name,
            String text,
            Set<Department> departments
    ) {}

    public record AskRequest(String question) {}

    @PostMapping("/ingest")
    public String ingest(@RequestBody IngestRequest request) {

        ingestionService.ingest(
                request.name(),
                request.text(),
                request.departments()
        );

        return "Document ingéré : " + request.name();
    }

    @PostMapping("/ingest-file")
    public String ingestFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "departments", required = false)
            Set<Department> departments
    ) {

        String text = pdfExtractionService.extractText(file);

        ingestionService.ingest(
                file.getOriginalFilename(),
                text,
                departments
        );

        return "Fichier ingéré : " + file.getOriginalFilename();
    }
}