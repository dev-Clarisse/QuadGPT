package com.QuadGPT.backend.rag.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.rag.Document;
import com.QuadGPT.backend.rag.DocumentFile;
import com.QuadGPT.backend.rag.TextChunker;
import com.QuadGPT.backend.rag.exception.DuplicateDocumentException;
import com.QuadGPT.backend.rag.repository.ChunkRepository;
import com.QuadGPT.backend.rag.repository.DocumentFileRepository;
import com.QuadGPT.backend.rag.repository.DocumentRepository;

@Service
public class IngestionService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DocumentRepository documentRepository;
    private final DocumentFileRepository documentFileRepository;
    private final ChunkRepository chunkRepository;
    private final TextChunker textChunker;
    private final EmbeddingService embeddingService;

    public IngestionService(
            DocumentRepository documentRepository,
            DocumentFileRepository documentFileRepository,
            ChunkRepository chunkRepository,
            TextChunker textChunker,
            EmbeddingService embeddingService
    ) {
        this.documentRepository = documentRepository;
        this.documentFileRepository = documentFileRepository;
        this.chunkRepository = chunkRepository;
        this.textChunker = textChunker;
        this.embeddingService = embeddingService;
    }

    /**
     * Outcome of an ingestion: either a new document, or an existing identical
     * file that was simply shared with additional departments.
     */
    public record IngestionResult(Document document, boolean alreadyExisted, Set<Department> addedDepartments) {}

    @Transactional
    public IngestionResult ingest(
            String documentName,
            String fullText,
            Set<Department> departments
    ) {
        return ingest(documentName, fullText, departments, null, null);
    }

    // Transactional so a failure (e.g. embedding service down) leaves no half-ingested document.
    @Transactional
    public IngestionResult ingest(
            String documentName,
            String fullText,
            Set<Department> departments,
            byte[] fileData,
            String contentType
    ) {
        Set<Department> requestedDepartments = departments == null ? Set.of() : departments;
        String fileHash = fileData == null ? null : sha256(fileData);

        // Same file content already ingested: never embed it twice.
        Optional<Document> existing = fileHash == null
                ? Optional.empty()
                : documentRepository.findByFileHash(fileHash);
        if (existing.isPresent()) {
            return shareWithDepartments(existing.get(), requestedDepartments);
        }

        Document document = documentRepository.save(
                new Document(documentName, requestedDepartments, fileHash)
        );

        if (fileData != null) {
            documentFileRepository.save(
                    new DocumentFile(document.getId(), contentType, fileData)
            );
        }

        for (String chunkText : textChunker.chunk(fullText)) {
            float[] embedding = embeddingService.embed(chunkText);

            chunkRepository.save(
                    document.getId(),
                    chunkText,
                    embedding
            );
        }

        return new IngestionResult(document, false, requestedDepartments);
    }

    private IngestionResult shareWithDepartments(Document document, Set<Department> requestedDepartments) {
        Set<Department> missing = new HashSet<>(requestedDepartments);
        missing.removeAll(document.getDepartments());

        if (missing.isEmpty()) {
            throw new DuplicateDocumentException(
                    "This document has already been uploaded as \"%s\" (%s, on %s)."
                            .formatted(
                                    document.getName(),
                                    document.getDepartments().stream()
                                            .map(Department::name)
                                            .sorted()
                                            .collect(Collectors.joining(", ")),
                                    document.getCreatedAt().format(DATE_FORMAT)
                            )
            );
        }

        document.addDepartments(missing);
        documentRepository.save(document);

        return new IngestionResult(document, true, missing);
    }

    private static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available on every JVM.
            throw new IllegalStateException(e);
        }
    }
}
