package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.request.SendMessageRequest;
import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.dto.response.SessionHistoryResponse;
import com.plataforma.conversacional.entity.Message;
import com.plataforma.conversacional.entity.Session;
import com.plataforma.conversacional.enums.MessageRole;
import com.plataforma.conversacional.enums.MessageStatus;
import com.plataforma.conversacional.event.MessageEventPublisher;
import com.plataforma.conversacional.exception.ResourceNotFoundException;
import com.plataforma.conversacional.mapper.MessageMapper;
import com.plataforma.conversacional.repository.MessageRepository;
import com.plataforma.conversacional.repository.SessionRepository;
import com.plataforma.conversacional.service.MessageService;
import com.plataforma.conversacional.strategy.MessageProcessingStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final MessageProcessingStrategy processingStrategy;
    private final MessageEventPublisher eventPublisher;

    public MessageServiceImpl(SessionRepository sessionRepository,
                              MessageRepository messageRepository,
                              MessageMapper messageMapper,
                              MessageProcessingStrategy processingStrategy,
                              MessageEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.processingStrategy = processingStrategy;
        this.eventPublisher = eventPublisher;
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

        String assistantContent = processingStrategy.process(request.content());

        Message assistantMessage = new Message();
        assistantMessage.setSession(session);
        assistantMessage.setContent(assistantContent);
        assistantMessage.setRole(MessageRole.ASSISTANT);
        assistantMessage.setStatus(MessageStatus.RECEIVED);
        messageRepository.save(assistantMessage);

        eventPublisher.publishMessageSent(assistantMessage.getId());

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
}