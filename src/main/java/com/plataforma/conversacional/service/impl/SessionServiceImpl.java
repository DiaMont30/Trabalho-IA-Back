package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.response.SessionResponse;
import com.plataforma.conversacional.entity.Session;
import com.plataforma.conversacional.enums.SessionStatus;
import com.plataforma.conversacional.exception.ResourceNotFoundException;
import com.plataforma.conversacional.mapper.SessionMapper;
import com.plataforma.conversacional.repository.SessionRepository;
import com.plataforma.conversacional.service.SessionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;

    public SessionServiceImpl(SessionRepository sessionRepository, SessionMapper sessionMapper) {
        this.sessionRepository = sessionRepository;
        this.sessionMapper = sessionMapper;
    }

    @Override
    public SessionResponse create() {
        Session session = new Session();
        session.setStatus(SessionStatus.ACTIVE);
        session.setTitle("Nova conversa");
        session = sessionRepository.save(session);
        return sessionMapper.toResponse(session);
    }

    @Override
    public SessionResponse findById(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        return sessionMapper.toResponse(session);
    }

    @Override
    public List<SessionResponse> findAll() {
        return sessionRepository.findAll().stream()
                .map(sessionMapper::toResponse)
                .toList();
    }
}