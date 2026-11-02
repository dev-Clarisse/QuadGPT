-- SHA-256 of the uploaded file, used to detect the same document being uploaded twice.
-- Documents ingested before this migration have no hash and are never matched.
ALTER TABLE document ADD COLUMN IF NOT EXISTS file_hash VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS uq_document_file_hash ON document (file_hash);
