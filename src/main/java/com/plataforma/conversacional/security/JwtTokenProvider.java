package com.plataforma.conversacional.security;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public String generateToken(String userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean validateToken(String token) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String getUserIdFromToken(String token) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
