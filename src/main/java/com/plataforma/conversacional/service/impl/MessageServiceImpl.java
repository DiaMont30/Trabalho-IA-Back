package com.plataforma.conversacional.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma.conversacional.dto.internal.FileUploadData;
import com.plataforma.conversacional.dto.request.SendMessageRequest;
import com.plataforma.conversacional.dto.response.DocumentResponse;
import com.plataforma.conversacional.dto.response.IngestionStatusResponse;
import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.dto.response.SessionHistoryResponse;
import com.plataforma.conversacional.entity.Document;
import com.plataforma.conversacional.entity.Message;
import com.plataforma.conversacional.entity.PipelineJob;
import com.plataforma.conversacional.entity.Session;
import com.plataforma.conversacional.entity.SourceReference;
import com.plataforma.conversacional.enums.MessageRole;
import com.plataforma.conversacional.enums.MessageStatus;
import com.plataforma.conversacional.enums.PipelineStatus;
import com.plataforma.conversacional.event.MessageEventPublisher;
import com.plataforma.conversacional.exception.ResourceNotFoundException;
import com.plataforma.conversacional.mapper.MessageMapper;
import com.plataforma.conversacional.pipeline.RagPipeline;
import com.plataforma.conversacional.pipeline.RagResult;
import com.plataforma.conversacional.repository.DocumentRepository;
import com.plataforma.conversacional.repository.MessageRepository;
import com.plataforma.conversacional.repository.PipelineJobRepository;
import com.plataforma.conversacional.repository.SessionRepository;
import com.plataforma.conversacional.repository.SourceReferenceRepository;
import com.plataforma.conversacional.service.DocumentService;
import com.plataforma.conversacional.service.MessageService;
import com.plataforma.conversacional.service.RagIngestionService;
import com.plataforma.conversacional.strategy.MessageProcessingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final MessageProcessingStrategy processingStrategy;
    private final MessageEventPublisher eventPublisher;
    private final RagPipeline ragPipeline;
    private final DocumentRepository documentRepository;
    private final PipelineJobRepository pipelineJobRepository;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final ObjectMapper objectMapper;
    private final DocumentService documentService;
    private final RagIngestionService ragIngestionService;

    public MessageServiceImpl(SessionRepository sessionRepository,
                              MessageRepository messageRepository,
                              MessageMapper messageMapper,
                              MessageProcessingStrategy processingStrategy,
                              MessageEventPublisher eventPublisher,
                              RagPipeline ragPipeline,
                              DocumentRepository documentRepository,
                              PipelineJobRepository pipelineJobRepository,
                              SourceReferenceRepository sourceReferenceRepository,
                              ObjectMapper objectMapper,
                              DocumentService documentService,
                              RagIngestionService ragIngestionService) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.processingStrategy = processingStrategy;
        this.eventPublisher = eventPublisher;
        this.ragPipeline = ragPipeline;
        this.documentRepository = documentRepository;
        this.pipelineJobRepository = pipelineJobRepository;
        this.sourceReferenceRepository = sourceReferenceRepository;
        this.objectMapper = objectMapper;
        this.documentService = documentService;
        this.ragIngestionService = ragIngestionService;
    }

    @Override
    @Transactional
    public MessageResponse send(Long sessionId, SendMessageRequest request) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found: " + sessionId));

        Message userMessage = new Message();
        userMessage.setSession(session);
        userMessage.setContent(request.content());
        userMessage.setRole(MessageRole.USER);
        userMessage.setStatus(MessageStatus.SENT);
        messageRepository.save(userMessage);

        updateSessionFromMessage(session, request.content());

        boolean hasIndexedDocuments = hasSessionIndexedDocuments(sessionId);

        String assistantContent;
        List<SourceReference> sourceRefs;
        String messageType;

        if (hasIndexedDocuments) {
            RagResult result = ragPipeline.execute(request.content(), sessionId);
            assistantContent = result.answer();
            sourceRefs = result.sources();
            messageType = "RAG";
        } else {
            assistantContent = processingStrategy.process(request.content());
            sourceRefs = List.of();
            messageType = "SIMPLE";
        }

        Message assistantMessage = new Message();
        assistantMessage.setSession(session);
        assistantMessage.setContent(assistantContent);
        assistantMessage.setRole(MessageRole.ASSISTANT);
        assistantMessage.setStatus(MessageStatus.RECEIVED);

        if (!sourceRefs.isEmpty()) {
            try {
                List<Map<String, Object>> sourcesMeta = sourceRefs.stream()
                        .map(ref -> {
                            Map<String, Object> m = new HashMap<>();
                            m.put("documentId", ref.getChunk().getDocument().getId());
                            m.put("documentName", ref.getChunk().getDocument().getOriginalName());
                            m.put("relevanceScore", ref.getRelevanceScore());
                            m.put("excerpt", ref.getExcerpt());
                            return m;
                        })
                        .toList();
                assistantMessage.setMetadata(objectMapper.writeValueAsString(sourcesMeta));
            } catch (Exception e) {
                log.warn("Failed to serialize source metadata", e);
            }

            for (SourceReference ref : sourceRefs) {
                ref.setMessage(assistantMessage);
                sourceReferenceRepository.save(ref);
            }
        }

        messageRepository.save(assistantMessage);

        eventPublisher.publishMessageSent(assistantMessage.getId(), messageType);

        return messageMapper.toResponse(assistantMessage);
    }

    private void updateSessionFromMessage(Session session, String content) {
        if ("Nova conversa".equals(session.getTitle())) {
            String newTitle = content.length() > 50
                ? content.substring(0, 50) + "..."
                : content;
            session.setTitle(newTitle);
        }
        session.setLastMessage(content.length() > 200
            ? content.substring(0, 200) + "..."
            : content);
        sessionRepository.save(session);
    }

    private boolean hasSessionIndexedDocuments(Long sessionId) {
        List<Document> documents = documentRepository.findBySessionId(sessionId);
        if (documents.isEmpty()) {
            return false;
        }
        for (Document doc : documents) {
            PipelineJob job = pipelineJobRepository.findByDocumentId(doc.getId()).orElse(null);
            if (job != null && job.getStatus() == PipelineStatus.READY) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public MessageResponse sendWithFiles(Long sessionId, SendMessageRequest request, MultipartFile[] files) throws IOException {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            FileUploadData uploadData = new FileUploadData(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getBytes(),
                    sessionId
            );
            DocumentResponse docResponse = documentService.store(uploadData);
            IngestionStatusResponse ingestResponse = ragIngestionService.ingestDocument(docResponse.id());
            awaitIngestion(ingestResponse.jobId());
        }

        Message userMessage = new Message();
        userMessage.setSession(session);
        userMessage.setContent(request.content());
        userMessage.setRole(MessageRole.USER);
        userMessage.setStatus(MessageStatus.SENT);
        messageRepository.save(userMessage);

        updateSessionFromMessage(session, request.content());

        RagResult result = ragPipeline.execute(request.content(), sessionId);

        Message assistantMessage = new Message();
        assistantMessage.setSession(session);
        assistantMessage.setContent(result.answer());
        assistantMessage.setRole(MessageRole.ASSISTANT);
        assistantMessage.setStatus(MessageStatus.RECEIVED);

        if (!result.sources().isEmpty()) {
            try {
                List<Map<String, Object>> sourcesMeta = result.sources().stream()
                        .map(ref -> {
                            Map<String, Object> m = new HashMap<>();
                            m.put("documentId", ref.getChunk().getDocument().getId());
                            m.put("documentName", ref.getChunk().getDocument().getOriginalName());
                            m.put("relevanceScore", ref.getRelevanceScore());
                            m.put("excerpt", ref.getExcerpt());
                            return m;
                        })
                        .toList();
                assistantMessage.setMetadata(objectMapper.writeValueAsString(sourcesMeta));
            } catch (Exception e) {
                log.warn("Failed to serialize source metadata", e);
            }

            for (SourceReference ref : result.sources()) {
                ref.setMessage(assistantMessage);
                sourceReferenceRepository.save(ref);
            }
        }

        messageRepository.save(assistantMessage);
        eventPublisher.publishMessageSent(assistantMessage.getId(), "RAG");

        return messageMapper.toResponse(assistantMessage);
    }

    private void awaitIngestion(Long jobId) {
        int maxRetries = 60;
        for (int i = 0; i < maxRetries; i++) {
            IngestionStatusResponse status = ragIngestionService.getStatus(jobId);
            if ("READY".equals(status.status())) return;
            if ("FAILED".equals(status.status()))
                throw new RuntimeException("Ingestion failed: " + status.errorMessage());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Ingestion wait interrupted", e);
            }
        }
        throw new RuntimeException("Ingestion timeout for job: " + jobId);
    }

    @Override
    public SessionHistoryResponse getHistory(Long sessionId, int page, int size) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepository.findBySessionId(sessionId, pageable);

        List<MessageResponse> messages = messagePage.getContent().stream()
                .map(messageMapper::toResponse)
                .toList();

        return new SessionHistoryResponse(
                sessionId,
                messages,
                messagePage.getNumber(),
                messagePage.getTotalPages(),
                messagePage.getTotalElements(),
                messagePage.hasNext()
        );
    }
}