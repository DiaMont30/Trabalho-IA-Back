package com.plataforma.conversacional.chunking;

public class Chunk {

    private final String content;
    private final int index;

    public Chunk(String content, int index) {
        this.content = content;
        this.index = index;
    }

    public String getContent() { return content; }
    public int getIndex() { return index; }
}
