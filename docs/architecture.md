# QuadGPT architecture

## Containers

Everything runs with `docker compose`. Only Traefik is exposed on port 80; the other
ports are bound to `127.0.0.1` for local development.

```mermaid
flowchart LR
    user([Browser])

    subgraph docker["docker compose · app-network"]
        traefik["Traefik<br/>:80"]
        frontend["Frontend<br/>React + Vite, served by Nginx"]
        backend["Backend<br/>Spring Boot 4 · Java 21"]
        db[("PostgreSQL 16<br/>+ pgvector")]
        ollama["local-llm<br/>Ollama"]
        init["ollama-init<br/>pulls the models, then exits"]
        minio[("MinIO<br/>not used by the code yet")]
        adminer["Adminer<br/>127.0.0.1:8082"]
    end

    user -->|"/"| traefik
    user -->|"/api/*"| traefik
    traefik -->|"/ (priority 1)"| frontend
    traefik -->|"/api/*"| backend
    backend -->|"JDBC · Flyway"| db
    backend -->|"/api/embed<br/>nomic-embed-text"| ollama
    backend -->|"/v1/chat/completions<br/>qwen2.5:1.5b"| ollama
    init -.->|"ollama pull"| ollama
    backend -.-> minio
    adminer --> db
```

## Backend layers

```mermaid
flowchart TB
    subgraph web["Controllers · /api"]
        auth["AuthController<br/>/auth/register · /auth/login"]
        users["UserController<br/>/users (ADMIN)"]
        chat["ChatController · ConversationController<br/>/chat · /conversations"]
        docs["DocumentController<br/>/documents"]
        rag["RagController<br/>/rag/ingest-file (ADMIN)"]
    end

    filter["JwtAuthenticationFilter<br/>reads the Bearer token"]
    errors["GlobalExceptionHandler<br/>exception → HTTP status + text"]

    subgraph services["Services"]
        authS["AuthService · JwtService"]
        userS["UserService"]
        chatS["ChatService<br/>ADMIN → all departments<br/>USER → own department"]
        ragS["RagService<br/>embed question → top 3 chunks → prompt"]
        ingS["IngestionService<br/>SHA-256 dedup → chunk → embed"]
        pdf["PdfExtractionService<br/>PDFBox"]
        chunker["TextChunker<br/>sentences, 500 chars, overlap"]
        docS["DocumentService"]
    end

    subgraph clients["External calls"]
        emb["EmbeddingService"]
        llm["LlmClientService"]
    end

    subgraph data["Repositories"]
        jpa["JPA repositories<br/>users · document · conversation · chat_message"]
        chunkRepo["ChunkRepository<br/>JdbcTemplate + pgvector"]
    end

    filter --> web
    auth --> authS
    users --> userS
    chat --> chatS --> ragS
    docs --> docS
    rag --> pdf
    rag --> ingS --> chunker
    ingS --> emb
    ingS --> chunkRepo
    ragS --> emb
    ragS --> chunkRepo
    ragS --> llm
    authS --> jpa
    userS --> jpa
    chatS --> jpa
    docS --> jpa
    ingS --> jpa
    web -.-> errors
```

## Asking a question (RAG)

```mermaid
sequenceDiagram
    actor U as User
    participant F as Frontend
    participant B as Backend
    participant DB as PostgreSQL + pgvector
    participant O as Ollama

    U->>F: types a question
    F->>B: POST /api/chat {message, conversationId}<br/>Authorization: Bearer JWT
    B->>B: JwtAuthenticationFilter checks the token
    B->>DB: load the user (role, department)
    B->>O: embed(question) with nomic-embed-text
    O-->>B: 768-dimension vector
    B->>DB: 3 closest chunks (cosine distance)<br/>USER: only their department's documents<br/>ADMIN: every document
    DB-->>B: chunks
    B->>O: prompt = instructions + chunks + question (qwen2.5)
    O-->>B: answer
    B->>DB: save conversation + user and assistant messages
    B-->>F: {response, conversationId, title}
```

## Uploading a document (ADMIN)

```mermaid
sequenceDiagram
    actor A as Admin
    participant B as Backend
    participant DB as PostgreSQL + pgvector
    participant O as Ollama

    A->>B: POST /api/rag/ingest-file (PDF + departments)
    B->>B: extract the text with PDFBox (400 if not a PDF)
    B->>DB: same SHA-256 already stored?
    alt same file, same departments
        B-->>A: 409 Conflict
    else same file, new department
        B->>DB: add the department, no new embedding
        B-->>A: 200 shared with the new departments
    else new file
        B->>DB: insert document, departments, original file
        loop every chunk (sentences, max 500 chars)
            B->>O: embed(chunk)
            B->>DB: insert chunk + vector
        end
        B-->>A: 200 ingested
    end
```

## CI/CD

```mermaid
flowchart LR
    push(["push / pull request on main"]) --> back["Backend tests<br/>unit, then integration<br/>(Testcontainers pgvector)"]
    push --> front["Frontend<br/>lint · Vitest · build"]
    back --> docker["Docker images<br/>built every time<br/>pushed to GHCR on main"]
    front --> docker
    docker --> deploy["Deploy over SSH<br/>only if DEPLOY_ENABLED = true"]
```
