package com.QuadGPT.backend.rag;

import java.util.List;

import org.springframework.stereotype.Service;

import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.llm.LlmClientService;

@Service
public class RagService {

    private final EmbeddingService embeddingService;
    private final ChunkRepository chunkRepository;
    private final LlmClientService llmClientService;

    public RagService(
            EmbeddingService embeddingService,
            ChunkRepository chunkRepository,
            LlmClientService llmClientService
    ) {
        this.embeddingService = embeddingService;
        this.chunkRepository = chunkRepository;
        this.llmClientService = llmClientService;
    }

    public String ask(String question, Department department) {

        float[] questionEmbedding = embeddingService.embed(question);

        List<String> relevantChunks =
                chunkRepository.findSimilarChunks(
                        questionEmbedding,
                        3,
                        department.name()
                );

        if (relevantChunks.isEmpty()) {
            return "Aucun document pertinent trouvé pour répondre à cette question.";
        }

        String context = String.join("\n\n", relevantChunks);

        String prompt = """
                Réponds à la question en te basant uniquement sur le contexte fourni.
                Si le contexte ne contient pas la réponse, dis-le clairement.

                Contexte :
                %s

                Question : %s
                """.formatted(context, question);

        return llmClientService.chat(prompt);
    }
}