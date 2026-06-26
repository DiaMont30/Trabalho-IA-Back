package com.plataforma.conversacional.dto.response;

import com.plataforma.conversacional.enums.SessionStatus;
import java.util.UUID;

public record SessionResponse(
    UUID id,
    String title,
    SessionStatus status,
    String createdAt,
    String updatedAt
) {}
