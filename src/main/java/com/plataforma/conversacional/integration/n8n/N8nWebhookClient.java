package com.plataforma.conversacional.integration.n8n;

import com.plataforma.conversacional.dto.response.RagQueryResponse;

public interface N8nWebhookClient {

    void notifyQueryCompleted(RagQueryResponse response, Long sessionId);

    void notifyIngestionCompleted(Long documentId, String status);
}
