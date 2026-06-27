package com.plataforma.conversacional.controller;

import com.plataforma.conversacional.dto.response.SessionPageResponse;
import com.plataforma.conversacional.dto.response.SessionResponse;
import com.plataforma.conversacional.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static com.plataforma.conversacional.constants.ApiConstants.API_VERSION;
import static com.plataforma.conversacional.constants.ApiConstants.DEFAULT_PAGE_SIZE;
import static com.plataforma.conversacional.constants.ApiConstants.SESSION_PATH;
import static com.plataforma.conversacional.constants.ApiConstants.SESSION_ID_VARIABLE;

@RestController
@RequestMapping(API_VERSION + SESSION_PATH)
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> create() {
        SessionResponse response = sessionService.create();
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{" + SESSION_ID_VARIABLE + "}")
    public ResponseEntity<SessionResponse> findById(@PathVariable Long sessionId) {
        SessionResponse response = sessionService.findById(sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<SessionPageResponse> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        SessionPageResponse response = sessionService.findAll(page, size);
        return ResponseEntity.ok(response);
    }
}