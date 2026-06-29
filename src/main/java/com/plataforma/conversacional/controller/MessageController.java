package com.plataforma.conversacional.controller;

import com.plataforma.conversacional.dto.request.SendMessageRequest;
import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.dto.response.SessionHistoryResponse;
import com.plataforma.conversacional.service.MessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
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
            @PathVariable Long sessionId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messageService.send(sessionId, request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping(value = "/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> sendWithFiles(
            @PathVariable Long sessionId,
            @RequestPart("content") @NotBlank @Size(min = 1, max = 5000) String content,
            @RequestPart("files") MultipartFile[] files) throws IOException {
        SendMessageRequest request = new SendMessageRequest(content);
        MessageResponse response = messageService.sendWithFiles(sessionId, request, files);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<SessionHistoryResponse> getHistory(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        SessionHistoryResponse response = messageService.getHistory(sessionId, page, size);
        return ResponseEntity.ok(response);
    }
}