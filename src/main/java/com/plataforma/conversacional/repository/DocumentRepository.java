package com.plataforma.conversacional.repository;

import com.plataforma.conversacional.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
}
