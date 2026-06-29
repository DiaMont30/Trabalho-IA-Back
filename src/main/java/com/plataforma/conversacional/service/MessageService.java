package com.plataforma.conversacional.service;

import com.plataforma.conversacional.dto.request.SendMessageRequest;
import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.dto.response.SessionHistoryResponse;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface MessageService {
    MessageResponse send(Long sessionId, SendMessageRequest request);
    MessageResponse sendWithFiles(Long sessionId, SendMessageRequest request, MultipartFile[] files) throws IOException;
    SessionHistoryResponse getHistory(Long sessionId, int page, int size);
}