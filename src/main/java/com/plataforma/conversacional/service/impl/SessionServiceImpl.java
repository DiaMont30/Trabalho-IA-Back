package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.response.SessionResponse;
import com.plataforma.conversacional.service.SessionService;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceImpl implements SessionService {

    @Override
    public SessionResponse create() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public SessionResponse findById(Long sessionId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}