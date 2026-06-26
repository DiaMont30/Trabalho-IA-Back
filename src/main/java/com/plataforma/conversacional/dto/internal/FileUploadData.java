package com.plataforma.conversacional.dto.internal;

import java.util.UUID;

public class FileUploadData {

    private String originalName;
    private String contentType;
    private long size;
    private byte[] content;
    private UUID sessionId;

    public FileUploadData(String originalName, String contentType, long size, byte[] content, UUID sessionId) {
        this.originalName = originalName;
        this.contentType = contentType;
        this.size = size;
        this.content = content;
        this.sessionId = sessionId;
    }

    public String getOriginalName() { return originalName; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
    public byte[] getContent() { return content; }
    public UUID getSessionId() { return sessionId; }
}
