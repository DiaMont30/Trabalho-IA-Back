package com.plataforma.conversacional.dto.response;

import java.util.List;
import java.util.UUID;

public record SessionHistoryResponse(
    UUID sessionId,
    List<MessageResponse> messages,
    int page,
    int totalPages,
    long totalElements,
    boolean hasNext
) {}
