package com.plataforma.conversacional.service;

import com.plataforma.conversacional.dto.response.SessionResponse;
import java.util.List;

public interface SessionService {
    SessionResponse create();
    SessionResponse findById(Long sessionId);
    List<SessionResponse> findAll();
}