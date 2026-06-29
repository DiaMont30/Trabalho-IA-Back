package com.plataforma.conversacional.mapper;

import com.plataforma.conversacional.dto.response.SessionResponse;
import com.plataforma.conversacional.entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SessionMapper {

    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "lastMessage", source = "lastMessage")
    SessionResponse toResponse(Session session);
}
