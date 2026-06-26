package com.plataforma.conversacional.service;

import com.plataforma.conversacional.dto.internal.FileUploadData;
import com.plataforma.conversacional.dto.response.DocumentResponse;

public interface DocumentService {
    DocumentResponse store(FileUploadData fileUploadData);
    DocumentResponse findById(Long documentId);
}