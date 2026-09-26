package com.QuadGPT.backend.rag;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.pgvector.PGvector;

@Repository
public class ChunkRepository {

    private final JdbcTemplate jdbcTemplate;

    public ChunkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(
            Long documentId,
            String content,
            float[] embedding
    ) {
        jdbcTemplate.update(
                "INSERT INTO document_chunk (document_id, content, embedding) VALUES (?, ?, ?)",
                documentId,
                content,
                new PGvector(embedding)
        );
    }

    public List<String> findSimilarChunks(
            float[] queryEmbedding,
            int limit,
            String department
    ) {
        return jdbcTemplate.query(
                """
                SELECT dc.content
                FROM document_chunk dc
                JOIN document_department dd
                    ON dd.document_id = dc.document_id
                WHERE dd.department = ?
                ORDER BY dc.embedding <=> ?
                LIMIT ?
                """,
                (rs, rowNum) -> rs.getString("content"),
                department,
                new PGvector(queryEmbedding),
                limit
        );
    }
}