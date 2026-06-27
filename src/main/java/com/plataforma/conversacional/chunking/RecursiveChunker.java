package com.plataforma.conversacional.chunking;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class RecursiveChunker implements ChunkingStrategy {

    private static final int MAX_CHUNK_SIZE = 512;
    private static final int MIN_CHUNK_SIZE = 50;

    @Override
    public List<Chunk> chunk(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        return chunkRecursive(text, 0);
    }

    private List<Chunk> chunkRecursive(String text, int index) {
        if (text.length() <= MAX_CHUNK_SIZE) {
            return List.of(new Chunk(text, index));
        }

        String[] paragraphs = text.split("\n\n");
        if (paragraphs.length > 1) {
            return splitBy(text, "\n\n", index);
        }

        String[] sentences = text.split("(?<=[.!?])\\s+");
        if (sentences.length > 1) {
            return splitBy(text, "(?<=[.!?])\\s+", index);
        }

        return chunkByCharacters(text, index);
    }

    private List<Chunk> splitBy(String text, String delimiter, int startIndex) {
        String[] parts = text.split(delimiter);
        List<Chunk> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int chunkIndex = startIndex;

        for (String part : parts) {
            if (current.length() + part.length() + 2 <= MAX_CHUNK_SIZE) {
                if (!current.isEmpty()) {
                    current.append("\n\n");
                }
                current.append(part.trim());
            } else {
                if (!current.isEmpty()) {
                    chunks.addAll(chunkRecursive(current.toString(), chunkIndex));
                    chunkIndex = chunks.size();
                }
                current = new StringBuilder(part.trim());
            }
        }

        if (!current.isEmpty()) {
            chunks.addAll(chunkRecursive(current.toString(), chunkIndex));
        }

        return chunks;
    }

    private List<Chunk> chunkByCharacters(String text, int startIndex) {
        List<Chunk> chunks = new ArrayList<>();
        int index = startIndex;

        for (int i = 0; i < text.length(); i += MAX_CHUNK_SIZE) {
            int end = Math.min(i + MAX_CHUNK_SIZE, text.length());
            if (end - i < MIN_CHUNK_SIZE && !chunks.isEmpty()) {
                Chunk last = chunks.remove(chunks.size() - 1);
                String merged = last.getContent() + text.substring(i, end);
                chunks.add(new Chunk(merged, last.getIndex()));
            } else {
                chunks.add(new Chunk(text.substring(i, end), index));
            }
            index++;
        }

        return chunks;
    }
}
