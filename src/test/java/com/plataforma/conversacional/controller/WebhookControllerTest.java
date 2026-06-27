package com.plataforma.conversacional.controller;

import com.plataforma.conversacional.dto.webhook.N8nWebhookPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
@WithMockUser
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void receiveRagResponse_ShouldReturn200_WhenValidPayload() throws Exception {
        N8nWebhookPayload payload = new N8nWebhookPayload(
                "QUERY_COMPLETED",
                Map.of("sessionId", 1L, "answer", "test"),
                "2024-01-01T00:00:00"
        );

        mockMvc.perform(post("/api/v1/webhooks/n8n/rag-response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void receiveRagResponse_ShouldReturn500_WhenEmptyBody() throws Exception {
        mockMvc.perform(post("/api/v1/webhooks/n8n/rag-response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void receiveRagResponse_ShouldReturn500_WhenInvalidJson() throws Exception {
        mockMvc.perform(post("/api/v1/webhooks/n8n/rag-response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid}")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}
