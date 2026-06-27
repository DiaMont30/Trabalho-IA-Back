package com.plataforma.conversacional.event;

import org.springframework.context.ApplicationEvent;

public class DocumentIngestedEvent extends ApplicationEvent {

    private final Long documentId;
    private final Long jobId;

    public DocumentIngestedEvent(Object source, Long documentId, Long jobId) {
        super(source);
        this.documentId = documentId;
        this.jobId = jobId;
    }

    public Long getDocumentId() { return documentId; }

    public Long getJobId() { return jobId; }
}
