package com.plataforma.conversacional.strategy;

import org.springframework.stereotype.Component;

@Component
public class MockMessageProcessingStrategy implements MessageProcessingStrategy {

    @Override
    public String process(String userMessage) {
        return "Recebi sua mensagem: \"" + userMessage
                + "\". Esta é uma resposta mockada do assistente.";
    }
}