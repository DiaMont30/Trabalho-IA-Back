package com.plataforma.conversacional.controller;

import com.plataforma.conversacional.dto.response.HealthResponse;
import com.plataforma.conversacional.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.plataforma.conversacional.constants.ApiConstants.API_VERSION;
import static com.plataforma.conversacional.constants.ApiConstants.HEALTH_PATH;

@RestController
@RequestMapping(API_VERSION + HEALTH_PATH)
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<HealthResponse> check() {
        HealthResponse response = healthService.check();
        return ResponseEntity.ok(response);
    }
}
