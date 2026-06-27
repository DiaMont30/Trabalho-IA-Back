package com.plataforma.conversacional.pipeline;

import com.plataforma.conversacional.entity.SourceReference;

import java.util.List;

public record RagResult(
    String answer,
    List<SourceReference> sources
) {}
