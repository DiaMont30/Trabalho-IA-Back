package com.plataforma.conversacional.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record UploadDocumentRequest(
    @NotNull MultipartFile file,
    Long sessionId
) {}