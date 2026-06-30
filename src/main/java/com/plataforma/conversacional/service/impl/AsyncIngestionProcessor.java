package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.chunking.Chunk;
import com.plataforma.conversacional.chunking.ChunkingStrategy;
import com.plataforma.conversacional.embedding.EmbeddingStrategy;
import com.plataforma.conversacional.entity.Document;
import com.plataforma.conversacional.entity.DocumentChunk;
import com.plataforma.conversacional.entity.PipelineJob;
import com.plataforma.conversacional.enums.DocumentType;
import com.plataforma.conversacional.enums.PipelineStatus;
import com.plataforma.conversacional.event.DocumentIngestedEvent;
import com.plataforma.conversacional.parsing.DocumentParser;
import com.plataforma.conversacional.repository.DocumentChunkRepository;
import com.plataforma.conversacional.repository.PipelineJobRepository;
import com.plataforma.conversacional.storage.FileStorageService;
import com.plataforma.conversacional.storage.VectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AsyncIngestionProcessor {

    private static final Logger log = LoggerFactory.getLogger(AsyncIngestionProcessor.class);

    private final DocumentChunkRepository documentChunkRepository;
    private final PipelineJobRepository pipelineJobRepository;
    private final FileStorageService fileStorageService;
    private final DocumentParser txtParser;
    private final DocumentParser pdfParser;
    private final ChunkingStrategy chunkingStrategy;
    private final EmbeddingStrategy embeddingStrategy;
    private final VectorStore vectorStore;
    private final ApplicationEventPublisher eventPublisher;

    public AsyncIngestionProcessor(
            DocumentChunkRepository documentChunkRepository,
            PipelineJobRepository pipelineJobRepository,
            FileStorageService fileStorageService,
            @Qualifier("txtParser") DocumentParser txtParser,
            @Qualifier("pdfParser") DocumentParser pdfParser,
            @Qualifier("fixedSizeChunker") ChunkingStrategy chunkingStrategy,
            EmbeddingStrategy embeddingStrategy,
            VectorStore vectorStore,
            ApplicationEventPublisher eventPublisher) {
        this.documentChunkRepository = documentChunkRepository;
        this.pipelineJobRepository = pipelineJobRepository;
        this.fileStorageService = fileStorageService;
        this.txtParser = txtParser;
        this.pdfParser = pdfParser;
        this.chunkingStrategy = chunkingStrategy;
        this.embeddingStrategy = embeddingStrategy;
        this.vectorStore = vectorStore;
        this.eventPublisher = eventPublisher;
    }

    public void processAsync(Document document, PipelineJob job) {
        try {
            job.setStatus(PipelineStatus.PARSING);
            pipelineJobRepository.save(job);

            byte[] content = readFileBytes(document.getStoragePath());

            DocumentParser parser = resolveParser(document.getType());
            String text = parser.parse(content, document.getContentType());

            job.setStatus(PipelineStatus.CHUNKING);
            pipelineJobRepository.save(job);

            List<Chunk> chunks = chunkingStrategy.chunk(text);

            List<DocumentChunk> documentChunks = new ArrayList<>();
            for (Chunk chunk : chunks) {
                DocumentChunk documentChunk = new DocumentChunk();
                documentChunk.setDocument(document);
                documentChunk.setContent(chunk.getContent());
                documentChunk.setChunkIndex(chunk.getIndex());
                documentChunks.add(documentChunk);
            }
            documentChunks = documentChunkRepository.saveAll(documentChunks);

            job.setStatus(PipelineStatus.EMBEDDING);
            pipelineJobRepository.save(job);

            List<String> texts = documentChunks.stream()
                    .map(DocumentChunk::getContent)
                    .toList();
            List<float[]> embeddings = embeddingStrategy.embedBatch(texts);

            vectorStore.storeChunks(documentChunks, embeddings);

            job.setChunksCount(documentChunks.size());
            job.setStatus(PipelineStatus.READY);
            job.setCompletedAt(LocalDateTime.now());
            pipelineJobRepository.save(job);

            eventPublisher.publishEvent(new DocumentIngestedEvent(this, document.getId(), job.getId()));

            log.info("Ingestion completed for documentId={}, jobId={}, chunksCount={}",
                    document.getId(), job.getId(), documentChunks.size());

        } catch (Exception e) {
            log.error("Ingestion failed for documentId={}, jobId={}", document.getId(), job.getId(), e);
            job.setStatus(PipelineStatus.FAILED);
            job.setErrorMessage(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            job.setCompletedAt(LocalDateTime.now());
            pipelineJobRepository.save(job);
        }
    }

    private DocumentParser resolveParser(DocumentType documentType) {
        return switch (documentType) {
            case PDF -> pdfParser;
            case TXT -> txtParser;
        };
    }

    private byte[] readFileBytes(String storagePath) {
        try (InputStream is = fileStorageService.retrieve(storagePath)) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content from: " + storagePath, e);
        }
    }
}
