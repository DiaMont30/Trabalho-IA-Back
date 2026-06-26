package com.plataforma.conversacional.service;

import com.plataforma.conversacional.dto.response.SessionResponse;

public interface SessionService {
    SessionResponse create();
    SessionResponse findById(Long sessionId);
}