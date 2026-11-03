package com.QuadGPT.backend.rag.controller;

import java.io.IOException;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.rag.service.IngestionService;
import com.QuadGPT.backend.rag.service.PdfExtractionService;
import com.QuadGPT.backend.rag.service.RagService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

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

    // At least one department, otherwise no USER could ever see the document.
    public record IngestRequest(
            @NotBlank String name,
            @NotBlank String text,
            @NotEmpty Set<Department> departments
    ) {}

    public record AskRequest(String question) {}

    @PostMapping("/ingest")
    @PreAuthorize("hasRole('ADMIN')")
    public String ingest(@Valid @RequestBody IngestRequest request) {

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
            @RequestParam("departments") Set<Department> departments
    ) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The file is empty");
        }

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