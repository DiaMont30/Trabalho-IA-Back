package com.plataforma.conversacional.chunking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class FixedSizeChunker implements ChunkingStrategy {

    private final int maxSize;
    private final int overlap;

    public FixedSizeChunker(
            @Value("${app.rag.chunking.max-size}") int maxSize,
            @Value("${app.rag.chunking.overlap}") int overlap) {
        this.maxSize = maxSize;
        this.overlap = overlap;
    }

    @Override
    public List<Chunk> chunk(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        List<Chunk> chunks = new ArrayList<>();
        int index = 0;
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + maxSize, text.length());
            String content = text.substring(start, end);
            chunks.add(new Chunk(content, index));
            index++;

            if (end >= text.length()) {
                break;
            }

            start = end - overlap;
        }

        return chunks;
    }
}
