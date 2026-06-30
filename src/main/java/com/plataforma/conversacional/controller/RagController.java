package com.plataforma.conversacional.controller;

import com.plataforma.conversacional.dto.request.RagQueryRequest;
import com.plataforma.conversacional.dto.response.IngestionStatusResponse;
import com.plataforma.conversacional.dto.response.RagQueryResponse;
import com.plataforma.conversacional.dto.response.SourceDetailResponse;
import com.plataforma.conversacional.entity.Document;
import com.plataforma.conversacional.entity.SourceReference;
import com.plataforma.conversacional.pipeline.RagPipeline;
import com.plataforma.conversacional.pipeline.RagResult;
import com.plataforma.conversacional.repository.DocumentRepository;
import com.plataforma.conversacional.repository.SourceReferenceRepository;
import com.plataforma.conversacional.service.RagIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static com.plataforma.conversacional.constants.ApiConstants.API_VERSION;
import static com.plataforma.conversacional.constants.ApiConstants.DOCUMENT_ID_VARIABLE;
import static com.plataforma.conversacional.constants.ApiConstants.INGEST_PATH;
import static com.plataforma.conversacional.constants.ApiConstants.QUERY_PATH;
import static com.plataforma.conversacional.constants.ApiConstants.RAG_PATH;
import static com.plataforma.conversacional.constants.ApiConstants.SOURCES_PATH;

@RestController
@RequestMapping(API_VERSION + RAG_PATH)
public class RagController {

    private final RagPipeline ragPipeline;
    private final RagIngestionService ragIngestionService;
    private final SourceReferenceRepository sourceReferenceRepository;
    private final DocumentRepository documentRepository;

    public RagController(RagPipeline ragPipeline,
                         RagIngestionService ragIngestionService,
                         SourceReferenceRepository sourceReferenceRepository,
                         DocumentRepository documentRepository) {
        this.ragPipeline = ragPipeline;
        this.ragIngestionService = ragIngestionService;
        this.sourceReferenceRepository = sourceReferenceRepository;
        this.documentRepository = documentRepository;
    }

    @PostMapping(QUERY_PATH)
    public ResponseEntity<RagQueryResponse> query(@Valid @RequestBody RagQueryRequest request) {
        String docNames = documentRepository.findBySessionId(request.sessionId()).stream()
            .map(Document::getOriginalName)
            .collect(Collectors.joining(", "));
        RagResult result = ragPipeline.execute(request.query(), docNames, request.sessionId());
        List<SourceDetailResponse> sources = result.sources().stream()
                .map(this::toSourceDetail)
                .toList();
        return ResponseEntity.ok(new RagQueryResponse(result.answer(), sources));
    }

    @PostMapping(INGEST_PATH + "/{" + DOCUMENT_ID_VARIABLE + "}")
    public ResponseEntity<IngestionStatusResponse> ingest(@PathVariable Long documentId) {
        IngestionStatusResponse response = ragIngestionService.ingestDocument(documentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(INGEST_PATH + "/{jobId}/status")
    public ResponseEntity<IngestionStatusResponse> status(@PathVariable Long jobId) {
        IngestionStatusResponse response = ragIngestionService.getStatus(jobId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(SOURCES_PATH + "/{messageId}")
    public ResponseEntity<List<SourceDetailResponse>> sources(@PathVariable Long messageId) {
        List<SourceReference> refs = sourceReferenceRepository.findByMessageId(messageId);
        List<SourceDetailResponse> response = refs.stream()
                .map(this::toSourceDetail)
                .toList();
        return ResponseEntity.ok(response);
    }

    private SourceDetailResponse toSourceDetail(SourceReference ref) {
        String documentName = ref.getChunk().getDocument().getOriginalName();
        return new SourceDetailResponse(
                ref.getChunk().getDocument().getId(),
                documentName,
                ref.getExcerpt(),
                ref.getRelevanceScore()
        );
    }
}
