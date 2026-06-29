package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.internal.FileUploadData;
import com.plataforma.conversacional.dto.response.DocumentResponse;
import com.plataforma.conversacional.entity.Document;
import com.plataforma.conversacional.entity.Session;
import com.plataforma.conversacional.enums.DocumentType;
import com.plataforma.conversacional.event.DocumentEventPublisher;
import com.plataforma.conversacional.exception.InvalidFileTypeException;
import com.plataforma.conversacional.exception.ResourceNotFoundException;
import com.plataforma.conversacional.mapper.DocumentMapper;
import com.plataforma.conversacional.repository.DocumentRepository;
import com.plataforma.conversacional.repository.SessionRepository;
import com.plataforma.conversacional.service.DocumentService;
import com.plataforma.conversacional.storage.FileStorageService;
import com.plataforma.conversacional.util.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final SessionRepository sessionRepository;
    private final FileStorageService fileStorageService;
    private final DocumentMapper documentMapper;
    private final DocumentEventPublisher documentEventPublisher;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               SessionRepository sessionRepository,
                               FileStorageService fileStorageService,
                               DocumentMapper documentMapper,
                               DocumentEventPublisher documentEventPublisher) {
        this.documentRepository = documentRepository;
        this.sessionRepository = sessionRepository;
        this.fileStorageService = fileStorageService;
        this.documentMapper = documentMapper;
        this.documentEventPublisher = documentEventPublisher;
    }

    @Override
    @Transactional
    public DocumentResponse store(FileUploadData fileUploadData) {
        String extension = FileUtils.getExtension(fileUploadData.getOriginalName());

        if (!FileUtils.isAllowedType(extension)) {
            throw new InvalidFileTypeException(
                    "File type not allowed: ." + extension + ". Accepted: PDF, TXT");
        }

        String uniqueFileName = FileUtils.generateUniqueFileName(fileUploadData.getOriginalName());
        String storagePath = fileStorageService.store(uniqueFileName, fileUploadData.getContent());

        DocumentType documentType = "pdf".equals(extension) ? DocumentType.PDF : DocumentType.TXT;

        Document document = new Document();
        document.setOriginalName(fileUploadData.getOriginalName());
        document.setStorageFileName(uniqueFileName);
        document.setStoragePath(storagePath);
        document.setType(documentType);
        document.setSize(fileUploadData.getSize());
        document.setContentType(fileUploadData.getContentType());

        if (fileUploadData.getSessionId() != null) {
            Session session = sessionRepository.findById(fileUploadData.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Session not found: " + fileUploadData.getSessionId()));
            document.setSession(session);
        }

        documentRepository.save(document);

        documentEventPublisher.publishDocumentUploaded(document.getId());

        return documentMapper.toResponse(document);
    }

    @Override
    public DocumentResponse findById(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found: " + documentId));
        return documentMapper.toResponse(document);
    }
}