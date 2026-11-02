-- Stores the original uploaded file so it can be downloaded later.
-- Kept in its own table so listing documents never loads file contents.
CREATE TABLE IF NOT EXISTS document_file (
    document_id  BIGINT PRIMARY KEY REFERENCES document(id) ON DELETE CASCADE,
    content_type VARCHAR(100) NOT NULL,
    data         BYTEA NOT NULL
);
