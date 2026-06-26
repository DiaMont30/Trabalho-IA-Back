package com.plataforma.conversacional.entity;

import com.plataforma.conversacional.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_document_session_id", columnList = "session_id"),
    @Index(name = "uk_document_storage_file_name", columnList = "storageFileName", unique = true)
})
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false, unique = true)
    private String storageFileName;

    @Column(nullable = false)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getStorageFileName() { return storageFileName; }
    public void setStorageFileName(String storageFileName) { this.storageFileName = storageFileName; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public DocumentType getType() { return type; }
    public void setType(DocumentType type) { this.type = type; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
