package com.plataforma.conversacional.embedding;

import java.util.List;

public interface EmbeddingStrategy {

    float[] embed(String text);

    List<float[]> embedBatch(List<String> texts);

    int getDimension();
}
