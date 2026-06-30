package com.plataforma.conversacional.strategy;

public interface MessageProcessingStrategy {
    String process(String userMessage);

    default String processWithContext(String context, String query) {
        return process("Conteúdo do(s) arquivo(s):\n" + context + "\n\nPergunta: " + query);
    }
}
