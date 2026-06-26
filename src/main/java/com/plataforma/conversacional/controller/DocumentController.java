package com.plataforma.conversacional.controller;

import com.plataforma.conversacional.dto.internal.FileUploadData;
import com.plataforma.conversacional.dto.response.DocumentResponse;
import com.plataforma.conversacional.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;
import static com.plataforma.conversacional.constants.ApiConstants.API_VERSION;
import static com.plataforma.conversacional.constants.ApiConstants.DOCUMENT_ID_VARIABLE;
import static com.plataforma.conversacional.constants.ApiConstants.DOCUMENT_PATH;

@RestController
@RequestMapping(API_VERSION + DOCUMENT_PATH)
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sessionId", required = false) UUID sessionId) throws IOException {
        FileUploadData fileUploadData = new FileUploadData(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getBytes(),
                sessionId
        );
        DocumentResponse response = documentService.store(fileUploadData);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{" + DOCUMENT_ID_VARIABLE + "}")
    public ResponseEntity<DocumentResponse> findById(@PathVariable UUID documentId) {
        DocumentResponse response = documentService.findById(documentId);
        return ResponseEntity.ok(response);
    }
}
