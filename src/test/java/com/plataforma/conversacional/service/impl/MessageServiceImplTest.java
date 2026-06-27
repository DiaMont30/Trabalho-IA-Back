package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.request.SendMessageRequest;
import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.dto.response.SessionHistoryResponse;
import com.plataforma.conversacional.entity.Message;
import com.plataforma.conversacional.entity.Session;
import com.plataforma.conversacional.enums.MessageRole;
import com.plataforma.conversacional.enums.MessageStatus;
import com.plataforma.conversacional.enums.SessionStatus;
import com.plataforma.conversacional.event.MessageEventPublisher;
import com.plataforma.conversacional.exception.ResourceNotFoundException;
import com.plataforma.conversacional.mapper.MessageMapper;
import com.plataforma.conversacional.repository.MessageRepository;
import com.plataforma.conversacional.repository.SessionRepository;
import com.plataforma.conversacional.strategy.MessageProcessingStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private MessageProcessingStrategy processingStrategy;

    @Mock
    private MessageEventPublisher eventPublisher;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Session createSession() {
        Session session = new Session();
        session.setId(1L);
        session.setStatus(SessionStatus.ACTIVE);
        return session;
    }

    @Test
    void send_ShouldSaveUserAndAssistantMessages_AndPublishEvent() {
        Session session = createSession();
        SendMessageRequest request = new SendMessageRequest("Hello");

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(processingStrategy.process("Hello")).thenReturn("Mock response");

        Message savedAssistant = new Message();
        savedAssistant.setId(2L);
        savedAssistant.setSession(session);
        savedAssistant.setContent("Mock response");
        savedAssistant.setRole(MessageRole.ASSISTANT);
        savedAssistant.setStatus(MessageStatus.RECEIVED);

        MessageResponse expectedResponse = new MessageResponse(
                2L, 1L, "Mock response", MessageRole.ASSISTANT,
                MessageStatus.RECEIVED, "2024-01-01T00:00:00", "2024-01-01T00:00:00");

        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            msg.setId(msg.getRole() == MessageRole.USER ? 1L : 2L);
            return msg;
        });
        when(messageMapper.toResponse(any(Message.class))).thenReturn(expectedResponse);

        MessageResponse result = messageService.send(1L, request);

        assertEquals("Mock response", result.content());
        verify(messageRepository, times(2)).save(any(Message.class));
        verify(processingStrategy).process("Hello");
        verify(eventPublisher).publishMessageSent(2L);
    }

    @Test
    void send_ShouldThrow_WhenSessionNotFound() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> messageService.send(1L, new SendMessageRequest("Hello")));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void getHistory_ShouldReturnPagedMessages() {
        when(sessionRepository.existsById(1L)).thenReturn(true);

        Message message = new Message();
        message.setId(1L);
        Page<Message> page = new PageImpl<>(List.of(message));

        MessageResponse msgResponse = new MessageResponse(
                1L, 1L, "Hello", MessageRole.USER,
                MessageStatus.SENT, "2024-01-01T00:00:00", "2024-01-01T00:00:00");

        when(messageRepository.findBySessionId(1L, PageRequest.of(0, 10))).thenReturn(page);
        when(messageMapper.toResponse(message)).thenReturn(msgResponse);

        SessionHistoryResponse result = messageService.getHistory(1L, 0, 10);

        assertEquals(1L, result.sessionId());
        assertEquals(1, result.messages().size());
        assertFalse(result.hasNext());
    }

    @Test
    void getHistory_ShouldThrow_WhenSessionNotFound() {
        when(sessionRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> messageService.getHistory(1L, 0, 10));
    }
}
