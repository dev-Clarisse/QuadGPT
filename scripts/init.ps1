# =========================================================
# QuadGPT initialization script
# Starts all Docker services and installs the local Ollama models.
# =========================================================

# Build and start all services defined in docker-compose.yml.
docker compose up -d --build

# Wait a few seconds so Ollama has time to start before pulling models.
Start-Sleep -Seconds 5

# Download the local chat model used for text generation.
docker exec quadgpt-local-llm ollama pull qwen2.5:1.5b

# Download the embedding model used by the RAG pipeline.
# This model converts document chunks and user queries into vectors
# that are stored and searched with PostgreSQL + pgvector.
docker exec quadgpt-local-llm ollama pull nomic-embed-text