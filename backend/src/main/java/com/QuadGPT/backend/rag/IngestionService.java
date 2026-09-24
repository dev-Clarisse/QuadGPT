package com.QuadGPT.backend.rag;

import org.springframework.stereotype.Service;

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

    public void ingest(String documentName, String fullText) {
        Document document = documentRepository.save(new Document(documentName));

        for (String chunkText : textChunker.chunk(fullText)) {
            float[] embedding = embeddingService.embed(chunkText);
            chunkRepository.save(document.getId(), chunkText, embedding);
        }
    }
}