package com.plataforma.conversacional.event;

import com.plataforma.conversacional.integration.n8n.N8nWebhookClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentIngestedEventListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestedEventListener.class);

    private final N8nWebhookClient n8nWebhookClient;

    public DocumentIngestedEventListener(N8nWebhookClient n8nWebhookClient) {
        this.n8nWebhookClient = n8nWebhookClient;
    }

    @EventListener
    public void handleDocumentIngested(DocumentIngestedEvent event) {
        log.info("DocumentIngested event received for documentId={}, jobId={}",
                event.getDocumentId(), event.getJobId());
        n8nWebhookClient.notifyIngestionCompleted(event.getDocumentId(), "COMPLETED");
    }
}
