package com.plataforma.conversacional.integration.n8n;

import com.plataforma.conversacional.event.DocumentIngestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class N8nNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(N8nNotificationListener.class);

    private final N8nWebhookClient n8nWebhookClient;

    public N8nNotificationListener(N8nWebhookClient n8nWebhookClient) {
        this.n8nWebhookClient = n8nWebhookClient;
    }

    @EventListener
    public void handleDocumentIngested(DocumentIngestedEvent event) {
        log.info("DocumentIngestedEvent received for documentId={}, jobId={} — notifying n8n",
                event.getDocumentId(), event.getJobId());

        n8nWebhookClient.notifyIngestionCompleted(event.getDocumentId(), "READY");
    }
}
