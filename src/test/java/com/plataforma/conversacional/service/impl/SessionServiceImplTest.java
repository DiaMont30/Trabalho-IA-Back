package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.response.SessionPageResponse;
import com.plataforma.conversacional.dto.response.SessionResponse;
import com.plataforma.conversacional.entity.Session;
import com.plataforma.conversacional.enums.SessionStatus;
import com.plataforma.conversacional.exception.ResourceNotFoundException;
import com.plataforma.conversacional.mapper.SessionMapper;
import com.plataforma.conversacional.repository.SessionRepository;
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
class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionMapper sessionMapper;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @Test
    void create_ShouldSaveAndReturnSessionResponse() {
        Session session = new Session();
        session.setId(1L);
        session.setStatus(SessionStatus.ACTIVE);
        session.setTitle("Nova conversa");

        SessionResponse expectedResponse = new SessionResponse(
                1L, "Nova conversa", null, SessionStatus.ACTIVE,
                "2024-01-01T00:00:00", "2024-01-01T00:00:00");

        when(sessionRepository.save(any(Session.class))).thenReturn(session);
        when(sessionMapper.toResponse(session)).thenReturn(expectedResponse);

        SessionResponse result = sessionService.create();

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(SessionStatus.ACTIVE, result.status());
        verify(sessionRepository).save(any(Session.class));
        verify(sessionMapper).toResponse(session);
    }

    @Test
    void findById_ShouldReturnSessionResponse_WhenExists() {
        Session session = new Session();
        session.setId(1L);

        SessionResponse expectedResponse = new SessionResponse(
                1L, "Test", null, SessionStatus.ACTIVE,
                "2024-01-01T00:00:00", "2024-01-01T00:00:00");

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionMapper.toResponse(session)).thenReturn(expectedResponse);

        SessionResponse result = sessionService.findById(1L);

        assertEquals(1L, result.id());
        verify(sessionRepository).findById(1L);
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sessionService.findById(1L));
    }

    @Test
    void findAll_ShouldReturnPagedResponse() {
        Session session = new Session();
        session.setId(1L);
        Page<Session> page = new PageImpl<>(List.of(session));

        SessionResponse sessionResponse = new SessionResponse(
                1L, "Test", null, SessionStatus.ACTIVE,
                "2024-01-01T00:00:00", "2024-01-01T00:00:00");

        when(sessionRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(sessionMapper.toResponse(session)).thenReturn(sessionResponse);

        SessionPageResponse result = sessionService.findAll(0, 10);

        assertEquals(1, result.sessions().size());
        assertEquals(0, result.page());
        assertEquals(1, result.totalPages());
        assertEquals(1, result.totalElements());
        assertFalse(result.hasNext());
    }

    @Test
    void findAll_ShouldReturnEmptyPage_WhenNoSessions() {
        Page<Session> emptyPage = new PageImpl<>(List.of());

        when(sessionRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        SessionPageResponse result = sessionService.findAll(0, 10);

        assertTrue(result.sessions().isEmpty());
        assertEquals(0, result.totalElements());
    }
}
