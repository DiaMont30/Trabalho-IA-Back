package com.plataforma.conversacional.service;

import com.plataforma.conversacional.dto.request.SendMessageRequest;
import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.dto.response.SessionHistoryResponse;

public interface MessageService {
    MessageResponse send(Long sessionId, SendMessageRequest request);
    SessionHistoryResponse getHistory(Long sessionId, int page, int size);
}