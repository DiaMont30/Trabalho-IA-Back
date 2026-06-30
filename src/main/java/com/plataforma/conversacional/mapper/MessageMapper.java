package com.plataforma.conversacional.mapper;

import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.dto.response.SourceDetailResponse;
import com.plataforma.conversacional.entity.Message;
import com.plataforma.conversacional.entity.SourceReference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageMapper {

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "sources", expression = "java(toSourceResponses(message.getSourceReferences()))")
    MessageResponse toResponse(Message message);

    default SourceDetailResponse toSourceResponse(SourceReference ref) {
        if (ref == null) return null;
        return new SourceDetailResponse(
                ref.getChunk().getDocument().getId(),
                ref.getChunk().getDocument().getOriginalName(),
                ref.getExcerpt(),
                ref.getRelevanceScore()
        );
    }

    default List<SourceDetailResponse> toSourceResponses(List<SourceReference> refs) {
        if (refs == null) return List.of();
        return refs.stream().map(this::toSourceResponse).toList();
    }
}
