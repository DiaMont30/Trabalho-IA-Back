package com.plataforma.conversacional.repository;

import com.plataforma.conversacional.entity.SourceReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SourceReferenceRepository extends JpaRepository<SourceReference, Long> {

    List<SourceReference> findByMessageId(Long messageId);
}
