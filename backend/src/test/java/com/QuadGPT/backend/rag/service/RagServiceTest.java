package com.QuadGPT.backend.rag.service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.llm.LlmClientService;
import com.QuadGPT.backend.rag.repository.ChunkRepository;

class RagServiceTest {

    private final float[] embedding = {0.1f, 0.2f};

    private EmbeddingService embeddingService;
    private ChunkRepository chunkRepository;
    private LlmClientService llmClientService;
    private RagService ragService;

    @BeforeEach
    void setUp() {
        embeddingService = mock(EmbeddingService.class);
        chunkRepository = mock(ChunkRepository.class);
        llmClientService = mock(LlmClientService.class);
        ragService = new RagService(embeddingService, chunkRepository, llmClientService);

        when(embeddingService.embed(anyString())).thenReturn(embedding);
    }

    @Test
    void searchesOnlyTheGivenDepartment() {
        when(chunkRepository.findSimilarChunks(any(), anyInt(), any())).thenReturn(List.of("contexte"));

        ragService.ask("question", Department.RH);

        verify(chunkRepository).findSimilarChunks(embedding, 3, "RH");
    }

    @Test
    void nullDepartmentSearchesEveryDocument() {
        when(chunkRepository.findSimilarChunks(any(), anyInt(), any())).thenReturn(List.of("contexte"));

        ragService.ask("question", null);

        verify(chunkRepository).findSimilarChunks(eq(embedding), eq(3), isNull());
    }

    @Test
    void sendsTheRetrievedChunksAndTheQuestionToTheLlm() {
        when(chunkRepository.findSimilarChunks(any(), anyInt(), any()))
                .thenReturn(List.of("Claire Martin dirige les RH.", "Les RH sont au deuxième étage."));
        when(llmClientService.chat(anyString())).thenReturn("Claire Martin");

        String answer = ragService.ask("Qui dirige les RH ?", Department.RH);

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(llmClientService).chat(prompt.capture());
        assertThat(prompt.getValue())
                .contains("Claire Martin dirige les RH.")
                .contains("Les RH sont au deuxième étage.")
                .contains("Qui dirige les RH ?");
        assertThat(answer).isEqualTo("Claire Martin");
    }

    @Test
    void doesNotCallTheLlmWhenNoDocumentMatches() {
        when(chunkRepository.findSimilarChunks(any(), anyInt(), any())).thenReturn(List.of());

        String answer = ragService.ask("question", Department.IT);

        assertThat(answer).contains("Aucun document pertinent");
        verify(llmClientService, never()).chat(anyString());
    }
}
