package com.plataforma.conversacional.controller;

import com.plataforma.conversacional.dto.request.SendMessageRequest;
import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.dto.response.SessionHistoryResponse;
import com.plataforma.conversacional.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;
import static com.plataforma.conversacional.constants.ApiConstants.API_VERSION;
import static com.plataforma.conversacional.constants.ApiConstants.DEFAULT_PAGE_SIZE;
import static com.plataforma.conversacional.constants.ApiConstants.MESSAGE_PATH;
import static com.plataforma.conversacional.constants.ApiConstants.SESSION_ID_VARIABLE;
import static com.plataforma.conversacional.constants.ApiConstants.SESSION_PATH;

@RestController
@RequestMapping(API_VERSION + SESSION_PATH + "/{sessionId}" + MESSAGE_PATH)
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> send(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messageService.send(sessionId, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<SessionHistoryResponse> getHistory(
            @PathVariable UUID sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        SessionHistoryResponse response = messageService.getHistory(sessionId, page, size);
        return ResponseEntity.ok(response);
    }
}
