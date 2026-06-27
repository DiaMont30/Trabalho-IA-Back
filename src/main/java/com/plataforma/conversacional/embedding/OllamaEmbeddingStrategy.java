package com.plataforma.conversacional.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma.conversacional.exception.EmbeddingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OllamaEmbeddingStrategy implements EmbeddingStrategy {

    private static final int NOMIC_EMBED_DIMENSION = 768;

    private final RestTemplate restTemplate;
    private final String url;
    private final String model;
    private final ObjectMapper objectMapper;

    public OllamaEmbeddingStrategy(
            RestTemplate restTemplate,
            @Value("${app.rag.ollama.url}") String url,
            @Value("${app.rag.ollama.model}") String model,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.url = url + "/api/embeddings";
        this.model = model;
        this.objectMapper = objectMapper;
    }

    @Override
    public float[] embed(String text) {
        Map<String, Object> request = Map.of(
                "model", model,
                "prompt", text
        );

        try {
            String response = restTemplate.postForObject(url, request, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode embeddingNode = root.get("embedding");

            if (embeddingNode == null || !embeddingNode.isArray()) {
                throw new EmbeddingException("Invalid embedding response from Ollama: missing 'embedding' field");
            }

            float[] embedding = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                embedding[i] = embeddingNode.get(i).floatValue();
            }
            return embedding;
        } catch (EmbeddingException e) {
            throw e;
        } catch (Exception e) {
            throw new EmbeddingException("Failed to generate embedding via Ollama", e);
        }
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        List<float[]> embeddings = new ArrayList<>(texts.size());
        for (String text : texts) {
            embeddings.add(embed(text));
        }
        return embeddings;
    }

    @Override
    public int getDimension() {
        return NOMIC_EMBED_DIMENSION;
    }
}
