package com.plataforma.conversacional.dto.webhook;

import java.util.Map;

public record N8nWebhookPayload(
    String eventType,
    Map<String, Object> payload,
    String timestamp
) {}
