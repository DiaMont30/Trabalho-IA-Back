package com.plataforma.conversacional.integration.n8n;

import com.plataforma.conversacional.dto.response.RagQueryResponse;
import com.plataforma.conversacional.dto.response.SourceDetailResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestN8nWebhookClientTest {

    @Mock
    private RestTemplate restTemplate;

    private final RagQueryResponse sampleResponse = new RagQueryResponse(
            "Resposta test",
            List.of(new SourceDetailResponse(1L, "doc.pdf", "excerpt", 0.95))
    );

    @Test
    void notifyQueryCompleted_ShouldPost_WhenEnabled() {
        RestN8nWebhookClient enabledClient = new RestN8nWebhookClient(restTemplate, "http://localhost:5678/webhook/rag", true);

        enabledClient.notifyQueryCompleted(sampleResponse, 1L);

        verify(restTemplate).postForObject(eq("http://localhost:5678/webhook/rag"), any(), eq(Void.class));
    }

    @Test
    void notifyQueryCompleted_ShouldSkip_WhenDisabled() {
        RestN8nWebhookClient disabledClient = new RestN8nWebhookClient(restTemplate, "http://localhost:5678/webhook/rag", false);

        disabledClient.notifyQueryCompleted(sampleResponse, 1L);

        verify(restTemplate, never()).postForObject(any(), any(), any());
    }

    @Test
    void notifyIngestionCompleted_ShouldPost_WhenEnabled() {
        RestN8nWebhookClient enabledClient = new RestN8nWebhookClient(restTemplate, "http://localhost:5678/webhook/rag", true);

        enabledClient.notifyIngestionCompleted(1L, "COMPLETED");

        verify(restTemplate).postForObject(eq("http://localhost:5678/webhook/rag"), any(), eq(Void.class));
    }

    @Test
    void notifyIngestionCompleted_ShouldSkip_WhenDisabled() {
        RestN8nWebhookClient disabledClient = new RestN8nWebhookClient(restTemplate, "http://localhost:5678/webhook/rag", false);

        disabledClient.notifyIngestionCompleted(1L, "COMPLETED");

        verify(restTemplate, never()).postForObject(any(), any(), any());
    }

    @Test
    void notifyQueryCompleted_ShouldSwallowException_WhenRestFails() {
        RestN8nWebhookClient enabledClient = new RestN8nWebhookClient(restTemplate, "http://localhost:5678/webhook/rag", true);

        when(restTemplate.postForObject(anyString(), any(), eq(Void.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertDoesNotThrow(() -> enabledClient.notifyQueryCompleted(sampleResponse, 1L));
        verify(restTemplate).postForObject(anyString(), any(), eq(Void.class));
    }

    @Test
    void notifyIngestionCompleted_ShouldSwallowException_WhenRestFails() {
        RestN8nWebhookClient enabledClient = new RestN8nWebhookClient(restTemplate, "http://localhost:5678/webhook/rag", true);

        when(restTemplate.postForObject(anyString(), any(), eq(Void.class)))
                .thenThrow(new RuntimeException("Timeout"));

        assertDoesNotThrow(() -> enabledClient.notifyIngestionCompleted(1L, "FAILED"));
        verify(restTemplate).postForObject(anyString(), any(), eq(Void.class));
    }
}
