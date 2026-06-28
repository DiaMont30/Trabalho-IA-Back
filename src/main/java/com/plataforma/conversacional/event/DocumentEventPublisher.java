package com.plataforma.conversacional.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DocumentEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public DocumentEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishDocumentUploaded(Long documentId) {
        eventPublisher.publishEvent(new DocumentUploadedEvent(this, documentId));
    }
}
