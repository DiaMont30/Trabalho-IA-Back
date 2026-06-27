package com.plataforma.conversacional.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RagQueryRequest(
    @NotBlank
    @Size(min = 1, max = 5000)
    String query,
    @NotNull
    Long sessionId
) {}
