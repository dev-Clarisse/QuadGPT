# QuadGPT

QuadGPT is a self-hosted enterprise chatbot developed during the EPF Hackathon.

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
          +--> Ollama

## First-time setup

Create a local `.env` file based on `.env.example`.

Then run:

    .\scripts\init.ps1

The script starts the Docker services and downloads the default Ollama model.

## Start manually

Start the project:

    docker compose up -d --build

Check running containers:

    docker ps

Stop the project:

    docker compose down

## Local services

- Application: `http://localhost`
- Adminer: `http://localhost:8082`
- MinIO: `http://localhost:9001`

## Security

- Secrets are stored in `.env`
- `.env` must not be committed
- PostgreSQL is not directly exposed
- Ollama is not directly exposed
- Internal services communicate through the Docker network

## Local LLM

Default development model:

    qwen2.5:1.5b

Test Ollama:

    docker exec -it quadgpt-ollama ollama run qwen2.5:1.5b "Réponds uniquement : QuadGPT fonctionne."

## Current infrastructure

Validated locally:

- Traefik
- Frontend
- Backend
- PostgreSQL + pgvector
- PostgreSQL healthcheck
- Adminer
- MinIO
- Ollama
- Persistent Docker volumes
- Internal Docker network

## Next steps

- Deploy on an Internet-accessible server
- Configure HTTPS
- Add CI/CD
- Final security review