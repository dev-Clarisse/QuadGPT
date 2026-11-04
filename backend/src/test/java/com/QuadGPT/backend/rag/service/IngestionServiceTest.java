package com.QuadGPT.backend.rag.service;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;

import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.rag.Document;
import com.QuadGPT.backend.rag.TextChunker;
import com.QuadGPT.backend.exception.DuplicateDocumentException;
import com.QuadGPT.backend.rag.repository.ChunkRepository;
import com.QuadGPT.backend.rag.repository.DocumentFileRepository;
import com.QuadGPT.backend.rag.repository.DocumentRepository;

class IngestionServiceTest {

    private DocumentRepository documentRepository;
    private DocumentFileRepository documentFileRepository;
    private ChunkRepository chunkRepository;
    private EmbeddingService embeddingService;
    private IngestionService ingestionService;

    @BeforeEach
    void setUp() {
        documentRepository = mock(DocumentRepository.class);
        documentFileRepository = mock(DocumentFileRepository.class);
        chunkRepository = mock(ChunkRepository.class);
        embeddingService = mock(EmbeddingService.class);
        ingestionService = new IngestionService(
                documentRepository, documentFileRepository, chunkRepository, new TextChunker(), embeddingService);

        when(embeddingService.embed(anyString())).thenReturn(new float[] {0.5f});
        when(documentRepository.findByFileHash(anyString())).thenReturn(Optional.empty());
        // Simulates the database assigning an id on insert.
        when(documentRepository.save(any())).thenAnswer(invocation -> {
            Document document = invocation.getArgument(0);
            ReflectionTestUtils.setField(document, "id", 7L);
            return document;
        });
    }

    @Test
    void embedsAndStoresEveryChunk() {
        String text = "Une phrase de test assez longue pour remplir un morceau. ".repeat(20);

        IngestionService.IngestionResult result =
                ingestionService.ingest("guide.pdf", text, Set.of(Department.RH), new byte[] {1}, "application/pdf");

        assertThat(result.alreadyExisted()).isFalse();
        int chunkCount = new TextChunker().chunk(text).size();
        assertThat(chunkCount).isGreaterThan(1);
        verify(embeddingService, times(chunkCount)).embed(anyString());
        verify(chunkRepository, times(chunkCount)).save(anyLong(), anyString(), any());
        verify(documentFileRepository).save(any());
    }

    @Test
    void sameFileForTheSameDepartmentIsRejected() {
        Document existing = new Document("RH.pdf", Set.of(Department.RH), "hash");
        when(documentRepository.findByFileHash(anyString())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> ingestionService.ingest(
                "RH.pdf", "texte", Set.of(Department.RH), new byte[] {1}, "application/pdf"))
                .isInstanceOf(DuplicateDocumentException.class)
                .hasMessageContaining("RH.pdf");
        verify(embeddingService, never()).embed(anyString());
    }

    @Test
    void sameFileForANewDepartmentIsSharedWithoutEmbeddingAgain() {
        Document existing = new Document("RH.pdf", Set.of(Department.RH), "hash");
        when(documentRepository.findByFileHash(anyString())).thenReturn(Optional.of(existing));

        IngestionService.IngestionResult result = ingestionService.ingest(
                "RH.pdf", "texte", Set.of(Department.RH, Department.DIRECTION), new byte[] {1}, "application/pdf");

        assertThat(result.alreadyExisted()).isTrue();
        assertThat(result.addedDepartments()).containsExactly(Department.DIRECTION);
        assertThat(existing.getDepartments()).containsExactlyInAnyOrder(Department.RH, Department.DIRECTION);
        verify(embeddingService, never()).embed(anyString());
    }
}
