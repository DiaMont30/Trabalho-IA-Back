package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.response.IngestionStatusResponse;
import com.plataforma.conversacional.entity.Document;
import com.plataforma.conversacional.entity.PipelineJob;
import com.plataforma.conversacional.enums.PipelineStatus;
import com.plataforma.conversacional.exception.ResourceNotFoundException;
import com.plataforma.conversacional.repository.DocumentRepository;
import com.plataforma.conversacional.repository.PipelineJobRepository;
import com.plataforma.conversacional.service.RagIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RagIngestionServiceImpl implements RagIngestionService {

    private static final Logger log = LoggerFactory.getLogger(RagIngestionServiceImpl.class);

    private final DocumentRepository documentRepository;
    private final PipelineJobRepository pipelineJobRepository;
    private final AsyncIngestionProcessor asyncIngestionProcessor;

    public RagIngestionServiceImpl(
            DocumentRepository documentRepository,
            PipelineJobRepository pipelineJobRepository,
            AsyncIngestionProcessor asyncIngestionProcessor) {
        this.documentRepository = documentRepository;
        this.pipelineJobRepository = pipelineJobRepository;
        this.asyncIngestionProcessor = asyncIngestionProcessor;
    }

    @Override
    @Transactional
    public IngestionStatusResponse ingestDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        PipelineJob job = new PipelineJob();
        job.setDocument(document);
        job.setStatus(PipelineStatus.QUEUED);
        job = pipelineJobRepository.save(job);

        asyncIngestionProcessor.processAsync(document, job);

        return toResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public IngestionStatusResponse getStatus(Long jobId) {
        PipelineJob job = pipelineJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline job not found: " + jobId));
        return toResponse(job);
    }

    private IngestionStatusResponse toResponse(PipelineJob job) {
        return new IngestionStatusResponse(
                job.getId(),
                job.getDocument().getId(),
                job.getStatus().name(),
                job.getChunksCount(),
                job.getErrorMessage()
        );
    }
}
