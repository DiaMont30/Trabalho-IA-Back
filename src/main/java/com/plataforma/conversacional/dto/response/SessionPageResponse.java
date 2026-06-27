package com.plataforma.conversacional.dto.response;

import java.util.List;

public record SessionPageResponse(
    List<SessionResponse> sessions,
    int page,
    int totalPages,
    long totalElements,
    boolean hasNext
) {}
