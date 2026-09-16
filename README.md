# QuadGPT

QuadGPT is a self-hosted enterprise chatbot developed during the EPF Hackathon.

The project combines a React frontend, a Spring Boot backend, PostgreSQL with pgvector, MinIO object storage, a local LLM service based on Ollama, and Traefik as a reverse proxy.

The main objective is to provide a self-hosted AI assistant while keeping the application infrastructure and local model execution under our control.

---

## Architecture

Browser
  |
  v
Traefik
  |
  +--> Frontend (React + Nginx)
  |
  +--> Backend (Spring Boot)
          |
          +--> PostgreSQL + pgvector
          +--> MinIO
          +--> local-llm (Ollama)

---

## Project structure

- `backend/`
  Spring Boot backend application.

- `frontend/`
  React frontend application served by Nginx.

- `docker-compose.yml`
  Defines all local Docker services and their communication:
  frontend, backend, PostgreSQL, MinIO, local LLM, Adminer and Traefik.

- `.env`
  Private local configuration containing passwords and secrets.
  This file must never be committed to Git.

- `.env.example`
  Public template showing which environment variables are required.

- `scripts/init.ps1`
  First-time initialization script.
  It starts the Docker services and downloads the default local LLM model.

- `Infos/Lancement_projet.md`
  Quick instructions for local development.

- `README.md`
  Main project and infrastructure documentation.

---

## First-time setup

After cloning the repository, create a local `.env` file based on `.env.example`.

Example variables:

    DB_NAME=quadgpt
    DB_USER=quadgpt
    DB_PASSWORD=your_password

    MINIO_ROOT_USER=admin
    MINIO_ROOT_PASSWORD=your_password

    JWT_SECRET=your_secret

    GLM_API_KEY=

The real `.env` file contains secrets and must not be committed.

Then run:

    .\scripts\init.ps1

The initialization script:

- builds the frontend and backend Docker images
- starts all Docker services
- waits for the local LLM service to start
- downloads the default local model `qwen2.5:1.5b`

---

## Main commands

### First-time initialization

    .\scripts\init.ps1

Starts the complete local environment and downloads the default local LLM model.

### Start the project

    docker compose up -d

Starts the existing containers.

### Rebuild after code changes

    docker compose up -d --build

Rebuilds the frontend and backend images before starting the services.

### Stop the project

    docker compose down

Stops the project containers without deleting persistent Docker volumes.

### Check running containers

    docker ps

Displays container status, names and exposed ports.

### Validate Docker Compose configuration

    docker compose config

Checks the final Docker Compose configuration and environment variable substitution.

---

## Local development access

The project supports both direct local development access and access through Traefik.

### Frontend

Direct local development access:

    http://localhost:3000

Main access through Traefik:

    http://localhost

### Backend

Direct local debugging access:

    http://localhost:8080

Backend access through Traefik:

    http://localhost/api

The direct backend port is bound to `127.0.0.1`, which means it is only accessible from the developer's own computer.

Every team member can use the same address:

    http://localhost:8080

No developer-specific IP address is required.

### Adminer

    http://localhost:8082

Adminer is used to inspect and manage the PostgreSQL database during development.

### MinIO console

    http://localhost:9001

The MinIO administration console is exposed only locally for development.

---

## Services

### Frontend

Container:

    quadgpt-frontend

Technology:

    React + Nginx

Role:

- provides the user interface
- serves the built React application
- can be accessed directly on `localhost:3000`
- can also be accessed through Traefik

---

### Backend

Container:

    quadgpt-backend

Technology:

    Spring Boot

Role:

- contains the application business logic
- communicates with PostgreSQL
- will communicate with MinIO
- will communicate with the local LLM service
- exposes backend APIs

Direct development access:

    http://localhost:8080

Traefik API route:

    http://localhost/api

---

### PostgreSQL + pgvector

Container:

    quadgpt-database

Technology:

    PostgreSQL + pgvector

Role:

- stores application data
- stores users and future conversations
- supports vector embeddings for future RAG features
- persists data using a Docker volume

The database is not directly exposed publicly.

A Docker healthcheck verifies that PostgreSQL is ready before dependent services start.

---

### Adminer

Container:

    quadgpt-adminer

Role:

- provides a web interface for PostgreSQL
- allows developers to inspect tables and stored data

Local access:

    http://localhost:8082

The database server name inside Docker is:

    database

---

### MinIO

Container:

    quadgpt-minio

Technology:

    MinIO

Role:

- provides S3-compatible object storage
- stores uploaded files and documents
- can be used for future RAG document storage
- can store generated files or other binary content

Local administration console:

    http://localhost:9001

The MinIO API is intended to be used internally by the backend.

---

### Local LLM

Container:

    quadgpt-local-llm

Internal service name:

    local-llm

Technology:

    Ollama

Role:

- runs open-weight language models locally
- allows QuadGPT to perform local AI inference
- avoids sending local inference requests to an external provider
- supports models such as Qwen, Llama or Mistral

The backend can communicate with the local LLM service through the internal Docker network:

    http://local-llm:11434

The default development model is:

    qwen2.5:1.5b

List installed models:

    docker exec -it quadgpt-local-llm ollama list

Download the default model manually:

    docker exec -it quadgpt-local-llm ollama pull qwen2.5:1.5b

Test local inference:

    docker exec -it quadgpt-local-llm ollama run qwen2.5:1.5b "Réponds uniquement : QuadGPT fonctionne."

Expected result:

    QuadGPT fonctionne.

---

### Traefik

Container:

    quadgpt-traefik

Technology:

    Traefik

Role:

- acts as the reverse proxy
- provides a single application entry point
- routes frontend and backend traffic
- discovers Docker services using container labels

Current routing:

    /      -> frontend
    /api   -> backend

The backend `/api` route has a higher priority than the frontend catch-all route.

This prevents `/api` requests from being handled by the frontend Nginx server.

---

## Environment variables

The project uses `.env` for local secrets and configuration.

The public template is:

    .env.example

The real local configuration is:

    .env

The `.env` file must never be committed because it can contain:

- database credentials
- MinIO credentials
- JWT secrets
- external LLM API keys

Example values in `.env.example` must use placeholders such as:

    change_me

---

## Security principles

The current local infrastructure follows several basic security principles:

- secrets are stored outside source code
- `.env` is excluded from Git
- PostgreSQL is not directly exposed to the host network
- the local LLM service is not directly exposed
- MinIO API traffic remains internal
- frontend and backend production-style traffic goes through Traefik
- backend and frontend direct development ports are bound to `127.0.0.1`
- internal services communicate through a Docker network
- Docker volumes are used for persistent data
- the Docker socket mounted into Traefik is read-only

---

## Persistent data

The project uses Docker volumes to preserve important data between container recreations.

Main persistent volumes:

- PostgreSQL data
- MinIO files
- local LLM models

This means downloaded models and stored data are not lost when containers are recreated.

Do not use:

    docker compose down -v

unless you intentionally want to delete persistent project data.

---

## Useful debugging commands

### Backend logs

    docker logs quadgpt-backend --tail 100

Useful for diagnosing:

- Spring Boot startup errors
- database connection problems
- application exceptions

### Frontend logs

    docker logs quadgpt-frontend --tail 100

Useful for diagnosing Nginx or frontend container problems.

### Traefik logs

    docker logs quadgpt-traefik --tail 100

Useful for diagnosing routing or Docker provider problems.

### PostgreSQL status

    docker ps

The database should show:

    healthy

### Local LLM models

    docker exec -it quadgpt-local-llm ollama list

---

## Current infrastructure status

The following components have been validated locally:

- Traefik reverse proxy
- React frontend
- Spring Boot backend
- PostgreSQL + pgvector
- PostgreSQL healthcheck
- Adminer database access
- MinIO object storage
- local LLM service
- `qwen2.5:1.5b` local model
- local LLM inference
- persistent Docker volumes
- internal Docker network
- direct frontend development access on port 3000
- direct backend debugging access on port 8080

---

## Current local URLs

Frontend:

    http://localhost:3000

Frontend through Traefik:

    http://localhost

Backend:

    http://localhost:8080

Backend through Traefik:

    http://localhost/api

Adminer:

    http://localhost:8082

MinIO:

    http://localhost:9001

---

## Next infrastructure steps

Planned infrastructure improvements:

- deploy QuadGPT on an Internet-accessible server
- configure HTTPS
- configure production secrets
- connect backend business logic to the local LLM service
- connect backend file handling to MinIO
- complete RAG integration with pgvector
- add CI/CD
- perform a final security review