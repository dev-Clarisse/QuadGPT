CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS document (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS document_department (
    document_id BIGINT NOT NULL REFERENCES document(id) ON DELETE CASCADE,
    department VARCHAR(50) NOT NULL,
    PRIMARY KEY (document_id, department)
);

CREATE TABLE IF NOT EXISTS document_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES document(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    embedding vector(768)
);

CREATE INDEX IF NOT EXISTS document_chunk_embedding_idx
    ON document_chunk USING hnsw (embedding vector_cosine_ops);