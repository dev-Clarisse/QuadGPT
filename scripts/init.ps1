# =========================================================
# QuadGPT initialization script
# Starts all Docker services and installs the default Ollama model.
# =========================================================

# Build and start all services defined in docker-compose.yml
docker compose up -d --build

# Wait a few seconds so Ollama has time to start
Start-Sleep -Seconds 5

# Download the default local LLM model used for development
docker exec quadgpt-ollama ollama pull qwen2.5:1.5b