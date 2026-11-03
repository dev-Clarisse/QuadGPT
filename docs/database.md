# QuadGPT database

PostgreSQL 16 with the `pgvector` extension. The schema is created by the Flyway
migrations in [backend/src/main/resources/db/migration](../backend/src/main/resources/db/migration)
(`V1` to `V5`); Hibernate never changes it (`ddl-auto: none`).

```mermaid
erDiagram
    users {
        BIGSERIAL id PK
        VARCHAR email UK "login"
        VARCHAR password "BCrypt hash"
        VARCHAR department "RH, FINANCE, IT, MARKETING, DIRECTION, SALES, SUPPORT"
        VARCHAR role "USER or ADMIN, default USER"
    }

    document {
        BIGSERIAL id PK
        VARCHAR name "original file name"
        TIMESTAMP created_at
        VARCHAR file_hash UK "SHA-256 of the file, detects duplicates"
    }

    document_department {
        BIGINT document_id PK, FK
        VARCHAR department PK "who may read the document"
    }

    document_chunk {
        BIGSERIAL id PK
        BIGINT document_id FK
        TEXT content "piece of text, max 500 characters"
        VECTOR embedding "vector(768), HNSW cosine index"
    }

    document_file {
        BIGINT document_id PK, FK
        VARCHAR content_type
        BYTEA data "original PDF, for download"
    }

    conversation {
        BIGSERIAL id PK
        BIGINT user_id FK
        VARCHAR title "first message, 60 characters max"
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    chat_message {
        BIGSERIAL id PK
        BIGINT conversation_id FK
        VARCHAR role "user or assistant"
        TEXT content
        TIMESTAMP created_at
    }

    document ||--|{ document_department : "shared with"
    document ||--o{ document_chunk : "split into"
    document ||--o| document_file : "original file"
    users ||--o{ conversation : "owns"
    conversation ||--o{ chat_message : "contains"
```

## Rules enforced by the schema

| Rule | How |
|---|---|
| One account per email | `users.email` is `UNIQUE` |
| The same file is never stored twice | unique index `uq_document_file_hash` on `document.file_hash` |
| Deleting a document removes its departments, chunks and file | `ON DELETE CASCADE` on the three foreign keys |
| Deleting a user removes their conversations and messages | `ON DELETE CASCADE` on `conversation.user_id` and `chat_message.conversation_id` |
| Vector search stays fast | HNSW index `document_chunk_embedding_idx` with `vector_cosine_ops` |
| The embedding model matches the column | `vector(768)`: `nomic-embed-text` produces 768 dimensions; another model needs a new migration |

## Indexes

| Index | Table | Used by |
|---|---|---|
| `document_chunk_embedding_idx` (HNSW, cosine) | `document_chunk` | similarity search of the RAG |
| `uq_document_file_hash` (unique) | `document` | duplicate upload detection |
| `idx_users_email` | `users` | login |
| `idx_conversation_user` (`user_id`, `updated_at DESC`) | `conversation` | conversation list in the sidebar |
| `idx_chat_message_conversation` (`conversation_id`, `id`) | `chat_message` | loading a conversation |

## Department access

`document_department` is the only link between documents and departments:

- a **USER** only searches and downloads documents with a row for their department;
- an **ADMIN** searches and downloads every document;
- a document shared with several departments has one row per department, and its chunks
  are stored only once.
