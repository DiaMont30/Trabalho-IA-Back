package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.response.HealthResponse;
import com.plataforma.conversacional.service.HealthService;
import org.springframework.stereotype.Service;

@Service
public class HealthServiceImpl implements HealthService {

    @Override
    public HealthResponse check() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
