package com.QuadGPT.backend.rag.repository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.rag.Document;
import com.QuadGPT.backend.support.IntegrationTest;

class ChunkRepositoryIT extends IntegrationTest {

    private static final String RH_CHUNK = "Claire Martin dirige les RH.";
    private static final String IT_CHUNK = "Le VPN est obligatoire.";
    private static final String SHARED_CHUNK = "La charte s'applique à tous.";

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @BeforeEach
    void storeChunks() {
        Long rh = documentRepository.save(new Document("RH.pdf", Set.of(Department.RH))).getId();
        Long it = documentRepository.save(new Document("IT.pdf", Set.of(Department.IT))).getId();
        Long shared = documentRepository.save(
                new Document("Charte.pdf", Set.of(Department.RH, Department.IT, Department.SALES))).getId();

        chunkRepository.save(rh, RH_CHUNK, vector(1, 0, 0));
        chunkRepository.save(it, IT_CHUNK, vector(0, 1, 0));
        chunkRepository.save(shared, SHARED_CHUNK, vector(0, 0, 1));
    }

    @Test
    void returnsTheClosestChunkFirst() {
        assertThat(chunkRepository.findSimilarChunks(vector(0, 1, 0), 1, "IT"))
                .containsExactly(IT_CHUNK);
    }

    @Test
    void neverReturnsChunksOfAnotherDepartment() {
        assertThat(chunkRepository.findSimilarChunks(vector(1, 0, 0), 10, "IT"))
                .containsExactlyInAnyOrder(IT_CHUNK, SHARED_CHUNK);
    }

    @Test
    void nullDepartmentSearchesEveryDocumentWithoutDuplicates() {
        // The shared document belongs to three departments but must come back only once.
        assertThat(chunkRepository.findSimilarChunks(vector(1, 0.5f, 0.2f), 10, null))
                .containsExactly(RH_CHUNK, IT_CHUNK, SHARED_CHUNK);
    }
}
