package com.plataforma.conversacional.event;

import com.plataforma.conversacional.service.RagIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentUploadedEventListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentUploadedEventListener.class);

    public DocumentUploadedEventListener() {
    }

    @EventListener
    public void handleDocumentUploaded(DocumentUploadedEvent event) {
        Long documentId = event.getDocumentId();
        log.info("DocumentUploaded event received for documentId={}. Ingestion will be triggered by the frontend explicitly via POST /api/v1/rag/ingest/{}", documentId, documentId);
    }
}
