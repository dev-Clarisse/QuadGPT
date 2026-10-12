package com.QuadGPT.backend.rag;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.QuadGPT.backend.department.Department;

@Service
public class IngestionService {

    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final TextChunker textChunker;
    private final EmbeddingService embeddingService;

    public IngestionService(
            DocumentRepository documentRepository,
            ChunkRepository chunkRepository,
            TextChunker textChunker,
            EmbeddingService embeddingService
    ) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.textChunker = textChunker;
        this.embeddingService = embeddingService;
    }

    public void ingest(
            String documentName,
            String fullText,
            Set<Department> departments
    ) {
        Document document = documentRepository.save(
                new Document(documentName, departments)
        );

        for (String chunkText : textChunker.chunk(fullText)) {
            float[] embedding = embeddingService.embed(chunkText);

            chunkRepository.save(
                    document.getId(),
                    chunkText,
                    embedding
            );
        }
    }
}