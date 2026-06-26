package com.plataforma.conversacional.service;

import com.plataforma.conversacional.dto.request.SendMessageRequest;
import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.dto.response.SessionHistoryResponse;
import java.util.UUID;

public interface MessageService {
    MessageResponse send(UUID sessionId, SendMessageRequest request);
    SessionHistoryResponse getHistory(UUID sessionId, int page, int size);
}
