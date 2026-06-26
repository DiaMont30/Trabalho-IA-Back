package com.plataforma.conversacional.mapper;

import com.plataforma.conversacional.dto.response.DocumentResponse;
import com.plataforma.conversacional.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMapper {

    @Mapping(target = "fileName", source = "storageFileName")
    @Mapping(target = "sessionId", expression = "java(document.getSession() != null ? document.getSession().getId() : null)")
    @Mapping(target = "uploadedAt", source = "uploadedAt")
    DocumentResponse toResponse(Document document);
}
