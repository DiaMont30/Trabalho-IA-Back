package com.plataforma.conversacional.mapper;

import com.plataforma.conversacional.dto.response.DocumentResponse;
import com.plataforma.conversacional.entity.Document;
import com.plataforma.conversacional.enums.DocumentType;
import java.time.format.DateTimeFormatter;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-27T12:44:09-0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class DocumentMapperImpl implements DocumentMapper {

    @Override
    public DocumentResponse toResponse(Document document) {
        if ( document == null ) {
            return null;
        }

        String fileName = null;
        String uploadedAt = null;
        Long id = null;
        String originalName = null;
        DocumentType type = null;
        Long size = null;
        String storagePath = null;

        fileName = document.getStorageFileName();
        if ( document.getUploadedAt() != null ) {
            uploadedAt = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format( document.getUploadedAt() );
        }
        id = document.getId();
        originalName = document.getOriginalName();
        type = document.getType();
        size = document.getSize();
        storagePath = document.getStoragePath();

        Long sessionId = document.getSession() != null ? document.getSession().getId() : null;

        DocumentResponse documentResponse = new DocumentResponse( id, fileName, originalName, type, size, storagePath, sessionId, uploadedAt );

        return documentResponse;
    }
}
