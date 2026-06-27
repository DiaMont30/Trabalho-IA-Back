package com.plataforma.conversacional.retrieval;

import java.util.List;

public interface Retriever {

    List<ScoredChunk> retrieve(String query, int topK);
}
