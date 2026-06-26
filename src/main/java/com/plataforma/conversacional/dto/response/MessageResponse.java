package com.plataforma.conversacional.dto.response;

import com.plataforma.conversacional.enums.MessageRole;
import com.plataforma.conversacional.enums.MessageStatus;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    UUID sessionId,
    String content,
    MessageRole role,
    MessageStatus status,
    String createdAt,
    String updatedAt
) {}
