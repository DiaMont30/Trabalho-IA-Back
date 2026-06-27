package com.plataforma.conversacional.repository;

import com.plataforma.conversacional.entity.PipelineJob;
import com.plataforma.conversacional.enums.PipelineStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PipelineJobRepository extends JpaRepository<PipelineJob, Long> {

    Optional<PipelineJob> findByDocumentId(Long documentId);

    List<PipelineJob> findByStatus(PipelineStatus status);
}
