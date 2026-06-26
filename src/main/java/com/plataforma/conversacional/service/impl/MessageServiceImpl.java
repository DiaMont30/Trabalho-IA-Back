package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.request.SendMessageRequest;
import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.dto.response.SessionHistoryResponse;
import com.plataforma.conversacional.service.MessageService;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class MessageServiceImpl implements MessageService {

    @Override
    public MessageResponse send(UUID sessionId, SendMessageRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public SessionHistoryResponse getHistory(UUID sessionId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
