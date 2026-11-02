package com.QuadGPT.backend.rag.controller;

import java.io.IOException;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.rag.service.IngestionService;
import com.QuadGPT.backend.rag.service.PdfExtractionService;
import com.QuadGPT.backend.rag.service.RagService;

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
    @PreAuthorize("hasRole('ADMIN')")
    public String ingest(@RequestBody IngestRequest request) {

        ingestionService.ingest(
                request.name(),
                request.text(),
                request.departments()
        );

        return "Document ingéré : " + request.name();
    }

    @PostMapping("/ingest-file")
    @PreAuthorize("hasRole('ADMIN')")
    public String ingestFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "departments", required = false)
            Set<Department> departments
    ) {

        String text = pdfExtractionService.extractText(file);

        byte[] fileData;
        try {
            fileData = file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire le fichier : " + file.getOriginalFilename(), e);
        }

        String contentType = file.getContentType() != null
                ? file.getContentType()
                : "application/pdf";

        IngestionService.IngestionResult result = ingestionService.ingest(
                file.getOriginalFilename(),
                text,
                departments,
                fileData,
                contentType
        );

        if (result.alreadyExisted()) {
            return "Fichier déjà présent (\"" + result.document().getName()
                    + "\"), ajouté aux départements : " + result.addedDepartments();
        }

        return "Fichier ingéré : " + file.getOriginalFilename();
    }
}