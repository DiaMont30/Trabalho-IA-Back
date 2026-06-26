package com.plataforma.conversacional.repository;

import com.plataforma.conversacional.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}