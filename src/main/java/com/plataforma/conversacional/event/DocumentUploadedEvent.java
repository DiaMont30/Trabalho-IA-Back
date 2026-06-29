package com.plataforma.conversacional.event;

import org.springframework.context.ApplicationEvent;

public class DocumentUploadedEvent extends ApplicationEvent {

    private final Long documentId;

    public DocumentUploadedEvent(Object source, Long documentId) {
        super(source);
        this.documentId = documentId;
    }

    public Long getDocumentId() { return documentId; }
}
