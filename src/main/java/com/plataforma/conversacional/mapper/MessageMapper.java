package com.plataforma.conversacional.mapper;

import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageMapper {

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "sources", ignore = true)
    MessageResponse toResponse(Message message);
}
