package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.internal.FileUploadData;
import com.plataforma.conversacional.dto.response.DocumentResponse;
import com.plataforma.conversacional.entity.Document;
import com.plataforma.conversacional.entity.Session;
import com.plataforma.conversacional.enums.DocumentType;
import com.plataforma.conversacional.enums.SessionStatus;
import com.plataforma.conversacional.exception.InvalidFileTypeException;
import com.plataforma.conversacional.exception.ResourceNotFoundException;
import com.plataforma.conversacional.mapper.DocumentMapper;
import com.plataforma.conversacional.repository.DocumentRepository;
import com.plataforma.conversacional.repository.SessionRepository;
import com.plataforma.conversacional.storage.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    void store_ShouldSaveDocument_WhenValidPdf() {
        FileUploadData data = new FileUploadData("doc.pdf", "application/pdf", 1024, "content".getBytes(), null);

        when(fileStorageService.store(anyString(), any())).thenReturn("/uploads/uuid-doc.pdf");

        Document savedDocument = new Document();
        savedDocument.setId(1L);
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        DocumentResponse expectedResponse = new DocumentResponse(
                1L, "uuid-doc.pdf", "doc.pdf", DocumentType.PDF, 1024L, "/uploads/uuid-doc.pdf", null, "2024-01-01T00:00:00");
        when(documentMapper.toResponse(any(Document.class))).thenReturn(expectedResponse);

        DocumentResponse result = documentService.store(data);

        assertEquals("doc.pdf", result.originalName());
        verify(fileStorageService).store(anyString(), any());
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void store_ShouldSaveDocument_WhenValidTxt() {
        FileUploadData data = new FileUploadData("note.txt", "text/plain", 512, "text".getBytes(), null);

        when(fileStorageService.store(anyString(), any())).thenReturn("/uploads/uuid-note.txt");

        Document savedDocument = new Document();
        savedDocument.setId(2L);
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        DocumentResponse expectedResponse = new DocumentResponse(
                2L, "uuid-note.txt", "note.txt", DocumentType.TXT, 512L, "/uploads/uuid-note.txt", null, "2024-01-01T00:00:00");
        when(documentMapper.toResponse(any(Document.class))).thenReturn(expectedResponse);

        DocumentResponse result = documentService.store(data);

        assertEquals(DocumentType.TXT, result.type());
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void store_ShouldThrow_WhenInvalidExtension() {
        FileUploadData data = new FileUploadData("image.png", "image/png", 100, "bad".getBytes(), null);

        assertThrows(InvalidFileTypeException.class, () -> documentService.store(data));
        verify(documentRepository, never()).save(any());
    }

    @Test
    void store_ShouldThrow_WhenNoExtension() {
        FileUploadData data = new FileUploadData("noext", "application/octet-stream", 100, "data".getBytes(), null);

        assertThrows(InvalidFileTypeException.class, () -> documentService.store(data));
    }

    @Test
    void store_ShouldAssociateSession_WhenSessionIdProvided() {
        Session session = new Session();
        session.setId(1L);
        session.setStatus(SessionStatus.ACTIVE);

        FileUploadData data = new FileUploadData("doc.pdf", "application/pdf", 1024, "content".getBytes(), 1L);

        when(fileStorageService.store(anyString(), any())).thenReturn("/path");
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Document savedDocument = new Document();
        savedDocument.setId(1L);
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        DocumentResponse expectedResponse = new DocumentResponse(
                1L, "uuid-doc.pdf", "doc.pdf", DocumentType.PDF, 1024L, "/path", 1L, "2024-01-01T00:00:00");
        when(documentMapper.toResponse(any(Document.class))).thenReturn(expectedResponse);

        DocumentResponse result = documentService.store(data);

        assertEquals(1L, result.sessionId());
        verify(sessionRepository).findById(1L);
    }

    @Test
    void store_ShouldThrow_WhenSessionNotFound() {
        FileUploadData data = new FileUploadData("doc.pdf", "application/pdf", 1024, "content".getBytes(), 999L);

        when(fileStorageService.store(anyString(), any())).thenReturn("/path");
        when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentService.store(data));
    }

    @Test
    void findById_ShouldReturnDocument_WhenExists() {
        Document document = new Document();
        document.setId(1L);

        DocumentResponse expectedResponse = new DocumentResponse(
                1L, "uuid.pdf", "doc.pdf", DocumentType.PDF, 1024L, "/path", null, "2024-01-01T00:00:00");

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(documentMapper.toResponse(document)).thenReturn(expectedResponse);

        DocumentResponse result = documentService.findById(1L);

        assertEquals(1L, result.id());
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentService.findById(1L));
    }
}
