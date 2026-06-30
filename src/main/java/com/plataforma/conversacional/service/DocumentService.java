package com.plataforma.conversacional.service;

import com.plataforma.conversacional.dto.internal.FileUploadData;
import com.plataforma.conversacional.dto.response.DocumentResponse;
import com.plataforma.conversacional.dto.response.DocumentStatusResponse;

public interface DocumentService {
    DocumentResponse store(FileUploadData fileUploadData);
    DocumentResponse findById(Long documentId);
    DocumentStatusResponse getDocumentStatus(Long documentId);
}