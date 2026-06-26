package com.plataforma.conversacional.exception;

import com.plataforma.conversacional.dto.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ResponseEntity<ErrorResponse> handleInvalidFileType(InvalidFileTypeException ex, WebRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, WebRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
