package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.request.SendMessageRequest;
import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.dto.response.SessionHistoryResponse;
import com.plataforma.conversacional.entity.Document;
import com.plataforma.conversacional.entity.Message;
import com.plataforma.conversacional.entity.Session;
import com.plataforma.conversacional.enums.MessageRole;
import com.plataforma.conversacional.enums.MessageStatus;
import com.plataforma.conversacional.event.MessageEventPublisher;
import com.plataforma.conversacional.exception.ResourceNotFoundException;
import com.plataforma.conversacional.mapper.MessageMapper;
import com.plataforma.conversacional.repository.DocumentRepository;
import com.plataforma.conversacional.repository.MessageRepository;
import com.plataforma.conversacional.repository.SessionRepository;
import com.plataforma.conversacional.service.MessageService;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final MessageProcessingStrategy processingStrategy;
    private final MessageEventPublisher eventPublisher;
    private final DocumentRepository documentRepository;

    public MessageServiceImpl(SessionRepository sessionRepository,
                              MessageRepository messageRepository,
                              MessageMapper messageMapper,
                              MessageProcessingStrategy processingStrategy,
                              MessageEventPublisher eventPublisher,
                              DocumentRepository documentRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.processingStrategy = processingStrategy;
        this.eventPublisher = eventPublisher;
        this.documentRepository = documentRepository;
    }

    @Override
    @Transactional
    public MessageResponse send(Long sessionId, SendMessageRequest request) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        Message userMessage = new Message();
        userMessage.setSession(session);
        userMessage.setContent(request.content());
        userMessage.setRole(MessageRole.USER);
        userMessage.setStatus(MessageStatus.SENT);
        messageRepository.save(userMessage);

        updateSessionFromMessage(session, request.content());

        String assistantContent = buildAndProcess(sessionId, request.content());
        String messageType = assistantContent.equals(request.content()) ? "SIMPLE" : "RAG";

        Message assistantMessage = new Message();
        assistantMessage.setSession(session);
        assistantMessage.setContent(assistantContent);
        assistantMessage.setRole(MessageRole.ASSISTANT);
        assistantMessage.setStatus(MessageStatus.RECEIVED);
        messageRepository.save(assistantMessage);

        eventPublisher.publishMessageSent(assistantMessage.getId(), messageType);

        return messageMapper.toResponse(assistantMessage);
    }

    @Override
    @Transactional
    public MessageResponse sendWithFiles(Long sessionId, SendMessageRequest request, MultipartFile[] files) throws IOException {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        Message userMessage = new Message();
        userMessage.setSession(session);
        userMessage.setContent(request.content());
        userMessage.setRole(MessageRole.USER);
        userMessage.setStatus(MessageStatus.SENT);
        messageRepository.save(userMessage);

        updateSessionFromMessage(session, request.content());

        String assistantContent = buildAndProcess(sessionId, request.content());

        Message assistantMessage = new Message();
        assistantMessage.setSession(session);
        assistantMessage.setContent(assistantContent);
        assistantMessage.setRole(MessageRole.ASSISTANT);
        assistantMessage.setStatus(MessageStatus.RECEIVED);
        messageRepository.save(assistantMessage);

        eventPublisher.publishMessageSent(assistantMessage.getId(), "RAG");

        return messageMapper.toResponse(assistantMessage);
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

    private String buildAndProcess(Long sessionId, String userQuestion) {
        List<Document> sessionDocuments = documentRepository.findBySessionId(sessionId);
        String context = sessionDocuments.stream()
                .filter(d -> d.getContent() != null && !d.getContent().isBlank())
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        if (context.isBlank()) {
            return processingStrategy.process(userQuestion);
        }

        String prompt = "Contexto do documento:\n\n" + context + "\n\n---\n\nPergunta do usuário: " + userQuestion;
        return processingStrategy.process(prompt);
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
}
