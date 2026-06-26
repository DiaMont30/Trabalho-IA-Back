package com.plataforma.conversacional.dto.response;

import java.util.List;

public record SessionHistoryResponse(
    Long sessionId,
    List<MessageResponse> messages,
    int page,
    int totalPages,
    long totalElements,
    boolean hasNext
) {}