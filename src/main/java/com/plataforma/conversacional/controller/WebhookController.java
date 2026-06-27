package com.plataforma.conversacional.controller;

import com.plataforma.conversacional.dto.webhook.N8nWebhookPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.plataforma.conversacional.constants.ApiConstants.API_VERSION;
import static com.plataforma.conversacional.constants.ApiConstants.N8N_PATH;
import static com.plataforma.conversacional.constants.ApiConstants.WEBHOOK_PATH;

@RestController
@RequestMapping(API_VERSION + WEBHOOK_PATH + N8N_PATH)
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    @PostMapping("/rag-response")
    public ResponseEntity<Void> receiveRagResponse(@RequestBody N8nWebhookPayload payload) {
        log.info("Received n8n webhook response: eventType={}, timestamp={}",
                payload.eventType(), payload.timestamp());
        return ResponseEntity.ok().build();
    }
}
