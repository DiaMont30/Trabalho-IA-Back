package com.plataforma.conversacional.dto.response;

import java.util.List;

public record ErrorResponse(
    int status,
    String error,
    String message,
    String timestamp,
    String path,
    List<String> validationErrors
) {}
