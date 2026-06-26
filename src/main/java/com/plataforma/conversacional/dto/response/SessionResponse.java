package com.plataforma.conversacional.dto.response;

import com.plataforma.conversacional.enums.SessionStatus;

public record SessionResponse(
    Long id,
    String title,
    SessionStatus status,
    String createdAt,
    String updatedAt
) {}