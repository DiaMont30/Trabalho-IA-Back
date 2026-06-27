package com.plataforma.conversacional.dto.response;

import java.util.List;

public record RagQueryResponse(
    String answer,
    List<SourceDetailResponse> sources
) {}
