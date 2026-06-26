package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.internal.FileUploadData;
import com.plataforma.conversacional.dto.response.DocumentResponse;
import com.plataforma.conversacional.service.DocumentService;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Override
    public DocumentResponse store(FileUploadData fileUploadData) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public DocumentResponse findById(UUID documentId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
