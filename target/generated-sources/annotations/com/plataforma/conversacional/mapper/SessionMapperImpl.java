package com.plataforma.conversacional.mapper;

import com.plataforma.conversacional.dto.response.SessionResponse;
import com.plataforma.conversacional.entity.Session;
import com.plataforma.conversacional.enums.SessionStatus;
import java.time.format.DateTimeFormatter;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-27T13:40:25-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.18 (Oracle Corporation)"
)
@Component
public class SessionMapperImpl implements SessionMapper {

    @Override
    public SessionResponse toResponse(Session session) {
        if ( session == null ) {
            return null;
        }

        String createdAt = null;
        String updatedAt = null;
        Long id = null;
        String title = null;
        SessionStatus status = null;

        if ( session.getCreatedAt() != null ) {
            createdAt = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format( session.getCreatedAt() );
        }
        if ( session.getUpdatedAt() != null ) {
            updatedAt = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format( session.getUpdatedAt() );
        }
        id = session.getId();
        title = session.getTitle();
        status = session.getStatus();

        SessionResponse sessionResponse = new SessionResponse( id, title, status, createdAt, updatedAt );

        return sessionResponse;
    }
}
