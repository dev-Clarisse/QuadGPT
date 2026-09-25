CREATE TABLE IF NOT EXISTS document (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    department VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS document_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES document(id) ON DELETE CASCADE,
    department VARCHAR(100),
    content TEXT NOT NULL,
    embedding vector(768)
);

ALTER TABLE document ADD COLUMN IF NOT EXISTS department VARCHAR(100);
ALTER TABLE document_chunk ADD COLUMN IF NOT EXISTS department VARCHAR(100);

CREATE INDEX IF NOT EXISTS document_chunk_embedding_idx
    ON document_chunk USING hnsw (embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS document_chunk_department_idx
    ON document_chunk (department);