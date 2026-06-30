package com.plataforma.conversacional.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma.conversacional.dto.request.SendMessageRequest;
import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.dto.response.SessionHistoryResponse;
import com.plataforma.conversacional.entity.Document;
import com.plataforma.conversacional.entity.DocumentChunk;
import com.plataforma.conversacional.entity.Message;
import com.plataforma.conversacional.entity.PipelineJob;
import com.plataforma.conversacional.entity.Session;
import com.plataforma.conversacional.entity.SourceReference;
import com.plataforma.conversacional.enums.DocumentType;
import com.plataforma.conversacional.enums.MessageRole;
import com.plataforma.conversacional.enums.MessageStatus;
import com.plataforma.conversacional.enums.PipelineStatus;
import com.plataforma.conversacional.enums.SessionStatus;
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
import static org.mockito.ArgumentMatchers.anyString;
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

    @Mock
    private RagPipeline ragPipeline;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private PipelineJobRepository pipelineJobRepository;

    @Mock
    private SourceReferenceRepository sourceReferenceRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Session createSession() {
        Session session = new Session();
        session.setId(1L);
        session.setStatus(SessionStatus.ACTIVE);
        return session;
    }

    private Document createDocument(Long id, Session session) {
        Document doc = new Document();
        doc.setId(id);
        doc.setOriginalName("test.pdf");
        doc.setSession(session);
        doc.setType(DocumentType.PDF);
        return doc;
    }

    @Test
    void send_ShouldUseProcessingStrategy_WhenNoIndexedDocuments() {
        Session session = createSession();
        SendMessageRequest request = new SendMessageRequest("Hello");

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(documentRepository.findBySessionId(1L)).thenReturn(List.of());
        when(processingStrategy.process("Hello")).thenReturn("Mock response");

        MessageResponse expectedResponse = new MessageResponse(
                2L, 1L, "Mock response", MessageRole.ASSISTANT,
                MessageStatus.RECEIVED, "2024-01-01T00:00:00", "2024-01-01T00:00:00", null);

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
        verify(eventPublisher).publishMessageSent(2L, "SIMPLE");
        verify(ragPipeline, never()).execute(anyString(), anyString(), anyLong());
    }

    @Test
    void send_ShouldUseRagPipeline_WhenSessionHasIndexedDocuments() throws Exception {
        Session session = createSession();
        SendMessageRequest request = new SendMessageRequest("Hello");
        Document doc = createDocument(10L, session);
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(100L);
        chunk.setDocument(doc);
        chunk.setContent("chunk content");

        SourceReference sourceRef = new SourceReference();
        sourceRef.setChunk(chunk);
        sourceRef.setRelevanceScore(0.95);
        sourceRef.setExcerpt("chunk content");

        RagResult ragResult = new RagResult("RAG response", List.of(sourceRef));

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(documentRepository.findBySessionId(1L)).thenReturn(List.of(doc));
        when(pipelineJobRepository.findByDocumentId(10L)).thenReturn(Optional.of(createReadyJob()));
        when(ragPipeline.execute("Hello", "test.pdf", 1L)).thenReturn(ragResult);
        when(objectMapper.writeValueAsString(any())).thenReturn("[{\"documentId\":10}]");

        MessageResponse expectedResponse = new MessageResponse(
                2L, 1L, "RAG response", MessageRole.ASSISTANT,
                MessageStatus.RECEIVED, "2024-01-01T00:00:00", "2024-01-01T00:00:00", null);

        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            msg.setId(msg.getRole() == MessageRole.USER ? 1L : 2L);
            return msg;
        });
        when(messageMapper.toResponse(any(Message.class))).thenReturn(expectedResponse);

        MessageResponse result = messageService.send(1L, request);

        assertEquals("RAG response", result.content());
        verify(messageRepository, times(2)).save(any(Message.class));
        verify(ragPipeline).execute("Hello", "test.pdf", 1L);
        verify(sourceReferenceRepository).save(sourceRef);
        verify(eventPublisher).publishMessageSent(2L, "RAG");
        verify(processingStrategy, never()).process(anyString());
    }

    private PipelineJob createReadyJob() {
        PipelineJob job = new PipelineJob();
        job.setId(1L);
        job.setStatus(PipelineStatus.READY);
        return job;
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
                MessageStatus.SENT, "2024-01-01T00:00:00", "2024-01-01T00:00:00", null);

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
