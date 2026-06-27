package com.plataforma.conversacional.service;

import com.plataforma.conversacional.dto.response.IngestionStatusResponse;

public interface RagIngestionService {

    IngestionStatusResponse ingestDocument(Long documentId);

    IngestionStatusResponse getStatus(Long jobId);
}
