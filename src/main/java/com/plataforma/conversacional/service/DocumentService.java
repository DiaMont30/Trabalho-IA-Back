package com.plataforma.conversacional.service;

import com.plataforma.conversacional.dto.internal.FileUploadData;
import com.plataforma.conversacional.dto.response.DocumentResponse;
import java.util.UUID;

public interface DocumentService {
    DocumentResponse store(FileUploadData fileUploadData);
    DocumentResponse findById(UUID documentId);
}
