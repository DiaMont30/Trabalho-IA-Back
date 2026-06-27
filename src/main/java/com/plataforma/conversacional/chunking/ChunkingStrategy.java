package com.plataforma.conversacional.chunking;

import java.util.List;

public interface ChunkingStrategy {

    List<Chunk> chunk(String text);
}
