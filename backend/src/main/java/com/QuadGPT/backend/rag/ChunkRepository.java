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

    public void save(Long documentId, String content, float[] embedding) {
        jdbcTemplate.update(
                "INSERT INTO document_chunk (document_id, content, embedding) VALUES (?, ?, ?)",
                documentId,
                content,
                new PGvector(embedding)
        );
    }

    public List<String> findSimilarChunks(float[] queryEmbedding, int limit) {
        return jdbcTemplate.query(
                "SELECT content FROM document_chunk ORDER BY embedding <=> ? LIMIT ?",
                (rs, rowNum) -> rs.getString("content"),
                new PGvector(queryEmbedding),
                limit
        );
    }
}