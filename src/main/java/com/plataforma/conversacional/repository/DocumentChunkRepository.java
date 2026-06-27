package com.plataforma.conversacional.repository;

import com.plataforma.conversacional.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentId(Long documentId);

    void deleteByDocumentId(Long documentId);
}
