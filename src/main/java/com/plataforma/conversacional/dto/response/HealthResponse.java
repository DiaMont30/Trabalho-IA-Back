package com.plataforma.conversacional.dto.response;

public record HealthResponse(
    String status,
    String database,
    String timestamp,
    String version
) {}
