package com.plataforma.conversacional.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@Primary
public class OllamaChatStrategy implements MessageProcessingStrategy {

    private final RestTemplate restTemplate;
    private final String url;
    private final String model;
    private final ObjectMapper objectMapper;

    public OllamaChatStrategy(
            RestTemplate restTemplate,
            @Value("${app.rag.ollama.url}") String baseUrl,
            @Value("${app.rag.ollama.chat-model}") String model,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.url = baseUrl + "/api/chat";
        this.model = model;
        this.objectMapper = objectMapper;
    }

    @Override
    public String process(String prompt) {
        Map<String, Object> request = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content",
                    "Você é um assistente especializado. Responda com base apenas no contexto fornecido. " +
                    "Se o contexto não tiver informações suficientes, diga que não sabe."),
                Map.of("role", "user", "content", prompt)
            ),
            "stream", false
        );
        return callOllama(request);
    }

    @Override
    public String processWithContext(String context, String query) {
        Map<String, Object> request = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content",
                    "Você é um assistente especializado em análise de documentos. " +
                    "Você receberá o CONTEÚDO DOS ARQUIVOS e depois uma pergunta. " +
                    "Responda APENAS com base no CONTEÚDO DOS ARQUIVOS fornecido. " +
                    "Se o conteúdo não tiver informações suficientes, diga que não sabe."),
                Map.of("role", "user", "content", "CONTEÚDO DOS ARQUIVOS:\n" + context),
                Map.of("role", "user", "content", "Com base SOMENTE no CONTEÚDO DOS ARQUIVOS acima, responda: " + query)
            ),
            "stream", false
        );
        return callOllama(request);
    }

    private String callOllama(Map<String, Object> request) {
        try {
            String response = restTemplate.postForObject(url, request, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode message = root.get("message");
            if (message == null) {
                throw new RuntimeException("Resposta inesperada do Ollama: campo 'message' ausente");
            }
            JsonNode content = message.get("content");
            if (content == null) {
                throw new RuntimeException("Resposta inesperada do Ollama: campo 'content' ausente");
            }
            return content.asText();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao chamar Ollama Chat API", e);
        }
    }
}
