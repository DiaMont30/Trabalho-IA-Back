package com.plataforma.conversacional.service;

import com.plataforma.conversacional.dto.response.SessionResponse;
import java.util.UUID;

public interface SessionService {
    SessionResponse create();
    SessionResponse findById(UUID sessionId);
}
