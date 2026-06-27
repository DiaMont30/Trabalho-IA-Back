#!/bin/bash
set -euo pipefail

echo "🚀 Starting Ollama container..."
docker compose up -d

echo "⏳ Waiting for Ollama to be ready..."
until curl -sf http://localhost:11434/api/tags > /dev/null 2>&1; do
  sleep 2
done

echo "📥 Pulling model nomic-embed-text..."
docker compose exec ollama ollama pull nomic-embed-text

echo "✅ Done — Ollama is ready with nomic-embed-text"
