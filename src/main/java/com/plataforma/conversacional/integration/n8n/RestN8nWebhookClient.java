package com.plataforma.conversacional.integration.n8n;

import com.plataforma.conversacional.dto.response.RagQueryResponse;
import com.plataforma.conversacional.dto.webhook.N8nWebhookPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class RestN8nWebhookClient implements N8nWebhookClient {

    private static final Logger log = LoggerFactory.getLogger(RestN8nWebhookClient.class);

    private final RestTemplate restTemplate;
    private final String webhookUrl;
    private final boolean enabled;

    public RestN8nWebhookClient(
            RestTemplate restTemplate,
            @Value("${app.n8n.webhook-url}") String webhookUrl,
            @Value("${app.n8n.enabled}") boolean enabled) {
        this.restTemplate = restTemplate;
        this.webhookUrl = webhookUrl;
        this.enabled = enabled;
    }

    @Override
    public void notifyQueryCompleted(RagQueryResponse response, Long sessionId) {
        if (!enabled) {
            log.debug("n8n webhook disabled - skipping query notification");
            return;
        }

        N8nWebhookPayload payload = new N8nWebhookPayload(
                "QUERY_COMPLETED",
                Map.of(
                        "sessionId", sessionId,
                        "answer", response.answer(),
                        "sources", response.sources()
                ),
                LocalDateTime.now().toString()
        );

        try {
            restTemplate.postForObject(webhookUrl, payload, Void.class);
            log.info("n8n webhook notified for query completion, sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("Failed to notify n8n webhook for query completion", e);
        }
    }

    @Override
    public void notifyIngestionCompleted(Long documentId, String status) {
        if (!enabled) {
            log.debug("n8n webhook disabled - skipping ingestion notification");
            return;
        }

        N8nWebhookPayload payload = new N8nWebhookPayload(
                "INGESTION_COMPLETED",
                Map.of(
                        "documentId", documentId,
                        "status", status
                ),
                LocalDateTime.now().toString()
        );

        try {
            restTemplate.postForObject(webhookUrl, payload, Void.class);
            log.info("n8n webhook notified for ingestion completion, documentId={}", documentId);
        } catch (Exception e) {
            log.error("Failed to notify n8n webhook for ingestion completion", e);
        }
    }
}
