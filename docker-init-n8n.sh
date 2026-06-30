#!/bin/bash
set -euo pipefail

echo "Starting n8n container..."
docker compose up -d n8n

echo "Waiting for n8n to be ready..."
until curl -sf http://localhost:5678/healthz > /dev/null 2>&1; do
  sleep 2
done

echo "n8n is ready at http://localhost:5678"
echo ""
echo "Next steps:"
echo "  1. Open http://localhost:5678 in your browser"
echo "  2. Create a new workflow with Webhook trigger (POST)"
echo "  3. Set the webhook URL to: http://localhost:5678/webhook/webhook/rag"
echo ""
echo "     Path no n8n: /webhook/rag"
echo ""
echo "  4. Add HTTP Request node to call:"
echo "     GET http://host.docker.internal:8080/api/v1/documents/{{ \$json.body.payload.documentId }}/status"
echo "  5. Configure your desired notification action (email, Telegram, etc.)"
echo "  6. Activate the workflow"
echo ""
echo "Then ensure the .env file has:"
echo "  N8N_WEBHOOK_URL=http://localhost:5678/webhook/webhook/rag"
