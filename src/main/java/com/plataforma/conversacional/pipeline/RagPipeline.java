package com.plataforma.conversacional.pipeline;

public interface RagPipeline {

    RagResult execute(String query, Long sessionId);
}
