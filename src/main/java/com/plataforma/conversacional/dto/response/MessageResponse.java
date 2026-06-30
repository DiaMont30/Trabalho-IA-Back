package com.plataforma.conversacional.dto.response;

import com.plataforma.conversacional.enums.MessageRole;
import com.plataforma.conversacional.enums.MessageStatus;

import java.util.List;

public record MessageResponse(
    Long id,
    Long sessionId,
    String content,
    MessageRole role,
    MessageStatus status,
    String createdAt,
    String updatedAt,
    String metadata,
    List<SourceDetailResponse> sources
) {}