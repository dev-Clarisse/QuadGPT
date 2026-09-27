# QuadGPT

QuadGPT is a self-hosted enterprise AI assistant developed during the EPF Hackathon.

The project combines a React frontend, a Spring Boot backend, PostgreSQL with pgvector, MinIO object storage, local AI models served by Ollama, and Traefik as a reverse proxy.

The main objective is to provide an enterprise-oriented AI assistant while keeping the application infrastructure, internal documents and local model execution under our control.

---

## Architecture

```text
Browser
   |
   v
Traefik
   |
   +--> Frontend
   |     |
   |     +--> Development: Vite :5173
   |     +--> Production: Nginx :80
   |
   +--> Backend (Spring Boot)
           |
           +--> PostgreSQL + pgvector
           |      |
           |      +--> Users
           |      +--> Documents
           |      +--> Embeddings
           |
           +--> MinIO
           |
           +--> Ollama
                  |
                  +--> qwen2.5:1.5b
                  |     Text generation
                  |
                  +--> nomic-embed-text
                        RAG embeddings
```

The frontend and backend are exposed through Traefik.

Internal infrastructure services communicate through the Docker network and are not intended to be directly exposed publicly.

---

## Main features

The current version of QuadGPT includes:

- user registration and authentication
- JWT-based authentication
- department-based user access
- authenticated chat
- local LLM inference through Ollama
- document ingestion for RAG
- PDF text extraction
- text chunking
- embedding generation
- PostgreSQL vector storage with pgvector
- semantic document retrieval
- department-aware RAG filtering
- answers generated from retrieved document context
- Docker-based local and production-style environments
- Traefik reverse proxy
- persistent PostgreSQL, MinIO and Ollama data

---

## Project structure

### `backend/`

Spring Boot backend application.

Main responsibilities:

- authentication
- JWT generation and validation
- user management
- chat API
- RAG ingestion
- document chunking
- embedding generation
- pgvector retrieval
- communication with the local LLM

### `frontend/`

React frontend application.

Development mode uses Vite.

Production mode builds the React application and serves the generated static files through Nginx.

### `docker-compose.yml`

Defines the production-style Docker infrastructure:

- frontend
- backend
- PostgreSQL + pgvector
- MinIO
- Ollama
- Adminer
- Traefik

### `docker-compose.override.yml`

Contains local development overrides.

In development:

- the frontend uses `Dockerfile.dev`
- Vite listens on container port `5173`
- `localhost:3000` forwards to Vite
- source files are mounted for development

### `.env`

Private local configuration containing passwords and secrets.

This file must never be committed to Git.

### `.env.example`

Public template showing which environment variables are required.

Only placeholders must be stored in this file.

### `scripts/init.ps1`

First-time initialization script.

It:

- builds the Docker services
- starts the complete environment
- waits for Ollama to start
- downloads the local chat model
- downloads the RAG embedding model

---

## First-time setup

Clone the repository and create a local `.env` file based on `.env.example`.

Example:

```env
DB_NAME=quadgpt
DB_USER=quadgpt_user
DB_PASSWORD=change_me

MINIO_ROOT_USER=admin
MINIO_ROOT_PASSWORD=change_me

JWT_SECRET=change_me_with_a_long_random_secret
```

`JWT_SECRET` must contain at least 32 characters.

The real `.env` file contains secrets and must never be committed.

Then run:

```powershell
.\scripts\init.ps1
```

The initialization script automatically downloads:

```text
qwen2.5:1.5b
nomic-embed-text
```

---

## Local AI models

QuadGPT currently uses two Ollama models for different purposes.

### `qwen2.5:1.5b`

Used for:

- text generation
- final chatbot responses
- RAG-based answers

### `nomic-embed-text`

Used for:

- document embeddings
- query embeddings
- semantic retrieval with pgvector

List installed models:

```powershell
docker exec quadgpt-local-llm ollama list
```

Expected models:

```text
qwen2.5:1.5b
nomic-embed-text
```

The Ollama service is available internally at:

```text
http://local-llm:11434
```

The backend can also use Ollama's OpenAI-compatible interface:

```text
http://local-llm:11434/v1
```

Ollama is not directly exposed to the host network.

---

## Authentication

QuadGPT uses JWT authentication.

Main endpoints:

```text
POST /api/auth/register
POST /api/auth/login
```

After a successful login, the backend returns a JWT.

Authenticated requests use:

```http
Authorization: Bearer <token>
```

The JWT secret is configured through:

```text
JWT_SECRET
```

and must remain outside source control.

---

## RAG

QuadGPT includes a Retrieval-Augmented Generation pipeline.

The workflow is:

```text
Document
   |
   v
Text extraction
   |
   v
Chunking
   |
   v
nomic-embed-text
   |
   v
Embedding
   |
   v
PostgreSQL + pgvector
   |
   v
Semantic search
   |
   v
Relevant context
   |
   v
qwen2.5:1.5b
   |
   v
Final answer
```

### Document ingestion

Text ingestion:

```text
POST /api/rag/ingest
```

PDF ingestion:

```text
POST /api/rag/ingest-file
```

Documents can be associated with one or more departments.

The current retrieval logic filters documents according to the authenticated user's department.

### Chat

Authenticated chat endpoint:

```text
POST /api/chat
```

The chat service:

1. receives the user's question
2. identifies the authenticated user's department
3. creates a query embedding
4. searches the most relevant document chunks using pgvector
5. builds a RAG context
6. sends the context and question to the local LLM
7. returns the generated answer

---

## PostgreSQL + pgvector

Container:

```text
quadgpt-database
```

PostgreSQL stores:

- users
- documents
- document departments
- document chunks
- vector embeddings

Main RAG tables:

```text
document
document_chunk
document_department
```

User data is stored in:

```text
users
```

The database uses pgvector for vector similarity search.

A Docker healthcheck verifies that PostgreSQL is ready before dependent services start.

Persistent data is stored in a Docker volume.

---

## MinIO

Container:

```text
quadgpt-minio
```

Technology:

```text
MinIO
```

MinIO provides S3-compatible object storage for the application.

It can be used for:

- uploaded documents
- generated files
- other binary application data

Internal API:

```text
http://minio:9000
```

Local administration console:

```text
http://localhost:9001
```

The administration console is bound to localhost for development.

---

## Frontend

### Development mode

Local development uses Vite.

Host access:

```text
http://localhost:3000
```

Docker mapping:

```text
127.0.0.1:3000
    |
    v
frontend:5173
```

The development configuration is defined in:

```text
docker-compose.override.yml
```

### Production-style mode

The production frontend is built with Vite and served by Nginx.

Traffic follows:

```text
Browser
   |
   v
Traefik :80
   |
   v
Nginx :80
   |
   v
React application
```

The production frontend does not require a direct host port.

---

## Traefik

Container:

```text
quadgpt-traefik
```

Traefik acts as the main reverse proxy.

Current routing:

```text
/       -> frontend
/api    -> backend
```

The backend API route has higher priority than the frontend catch-all route.

This prevents `/api` requests from being handled by the frontend.

Main application entry point:

```text
http://localhost
```

---

## Nginx

The production frontend uses Nginx.

Static files generated by Vite are served from:

```text
/usr/share/nginx/html
```

Unknown frontend routes fall back to:

```text
/index.html
```

This supports client-side routing in the React single-page application.

Nginx can also forward `/api` traffic to the backend through the Docker network.

---

## Main commands

### First-time initialization

```powershell
.\scripts\init.ps1
```

Starts the environment and downloads both required Ollama models.

### Start development environment

```powershell
docker compose up -d
```

Docker Compose automatically applies:

```text
docker-compose.override.yml
```

### Rebuild after changes

```powershell
docker compose up -d --build
```

### Start production-style environment locally

```powershell
docker compose -f docker-compose.yml up -d --build
```

This does not apply the development override.

The frontend therefore runs through Nginx instead of Vite.

### Stop the project

```powershell
docker compose down
```

This stops containers without deleting persistent volumes.

Do not use:

```powershell
docker compose down -v
```

unless persistent application data and downloaded Ollama models should intentionally be deleted.

### Check containers

```powershell
docker ps
```

### Validate Compose configuration

```powershell
docker compose config
```

---

## Local URLs

### Main application through Traefik

```text
http://localhost
```

### Development frontend

```text
http://localhost:3000
```

### Backend direct debugging access

```text
http://localhost:8080
```

### Backend through Traefik

```text
http://localhost/api
```

### Adminer

```text
http://localhost:8082
```

### MinIO console

```text
http://localhost:9001
```

---

## Docker networking

The application uses an internal Docker network:

```text
app-network
```

Service-to-service communication uses Docker service names.

Examples:

```text
backend:8080
database:5432
minio:9000
local-llm:11434
```

Internal services do not need public IP addresses.

---

## Security principles

The current infrastructure applies the following security principles:

- secrets are stored outside source code
- `.env` is excluded from Git
- `.env.example` contains placeholders only
- JWT tokens are signed using a secret stored in the environment
- PostgreSQL direct development access is bound to `127.0.0.1`
- backend direct debugging access is bound to `127.0.0.1`
- Adminer is bound to `127.0.0.1`
- MinIO administration is bound to `127.0.0.1`
- Ollama is not directly exposed to the host
- production-style application traffic goes through Traefik
- internal services communicate through the Docker network
- persistent data is stored in Docker volumes
- the Docker socket used by Traefik is mounted read-only
- production passwords and JWT secrets must use strong random values

Production environments must not reuse development passwords.

---

## Persistent data

Persistent Docker volumes are used for:

- PostgreSQL data
- MinIO data
- Ollama models

This means database content and downloaded models survive normal container recreation.

---

## Useful debugging commands

### Backend logs

```powershell
docker logs quadgpt-backend --tail 100
```

Useful for:

- Spring Boot startup errors
- JWT configuration problems
- database connection errors
- application exceptions

### Frontend logs

```powershell
docker logs quadgpt-frontend --tail 100
```

Useful for:

- Vite startup issues
- Nginx errors
- static file problems

### Traefik logs

```powershell
docker logs quadgpt-traefik --tail 100
```

Useful for routing and Docker provider problems.

### Installed Ollama models

```powershell
docker exec quadgpt-local-llm ollama list
```

### PostgreSQL tables

```powershell
docker exec -it quadgpt-database psql -U quadgpt_user -d quadgpt -c "\dt"
```

---

## Validated local workflow

The following complete workflow has been tested locally:

```text
User registration
      |
      v
Login
      |
      v
JWT
      |
      v
React frontend
      |
      v
/api/chat
      |
      v
Spring Boot
      |
      v
RAG retrieval
      |
      v
PostgreSQL + pgvector
      |
      v
Relevant document context
      |
      v
Ollama / qwen2.5:1.5b
      |
      v
Generated answer
```

A RAG test document was successfully:

- ingested
- embedded using `nomic-embed-text`
- stored in PostgreSQL
- retrieved through pgvector
- filtered by department
- used as context for the local LLM

The complete frontend-to-RAG-to-LLM workflow has been validated in both:

- Vite development mode
- production-style Nginx + Traefik mode

---

## Current status

Validated components:

- React frontend
- Vite development environment
- Nginx production frontend
- Spring Boot backend
- user registration
- login
- JWT authentication
- PostgreSQL
- pgvector
- document ingestion
- PDF extraction
- document chunking
- `nomic-embed-text`
- vector storage
- semantic retrieval
- department filtering
- authenticated chat
- local Ollama inference
- `qwen2.5:1.5b`
- Traefik reverse proxy
- Docker internal networking
- persistent volumes
- development and production-style Docker configurations

---

## Remaining infrastructure work

Before final production deployment, the main remaining infrastructure tasks are:

- verify the final VPS deployment
- configure or verify production secrets
- configure HTTPS if available
- verify CI/CD deployment
- complete final production security checks
- verify MinIO integration with the final document workflow if required

---

## Team workflow

Development follows a branch-based workflow:

```text
feature/fix branch
      |
      v
Pull Request
      |
      v
develop
      |
      v
validation
      |
      v
main
      |
      v
production
```

Infrastructure changes should be tested locally before being merged into `develop`.

---

## QuadGPT

QuadGPT demonstrates how an enterprise-oriented AI assistant can combine:

- local AI inference
- RAG
- vector search
- authentication
- containerized infrastructure
- reverse proxy routing
- persistent data
- controlled internal networking

while keeping the core infrastructure and local model execution under the team's control.