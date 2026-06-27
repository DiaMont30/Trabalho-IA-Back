package com.plataforma.conversacional.mapper;

import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.entity.Message;
import com.plataforma.conversacional.entity.Session;
import com.plataforma.conversacional.enums.MessageRole;
import com.plataforma.conversacional.enums.MessageStatus;
import java.time.format.DateTimeFormatter;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-27T10:58:27-0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class MessageMapperImpl implements MessageMapper {

    @Override
    public MessageResponse toResponse(Message message) {
        if ( message == null ) {
            return null;
        }

        Long sessionId = null;
        String createdAt = null;
        String updatedAt = null;
        Long id = null;
        String content = null;
        MessageRole role = null;
        MessageStatus status = null;

        sessionId = messageSessionId( message );
        if ( message.getCreatedAt() != null ) {
            createdAt = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format( message.getCreatedAt() );
        }
        if ( message.getUpdatedAt() != null ) {
            updatedAt = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format( message.getUpdatedAt() );
        }
        id = message.getId();
        content = message.getContent();
        role = message.getRole();
        status = message.getStatus();

        MessageResponse messageResponse = new MessageResponse( id, sessionId, content, role, status, createdAt, updatedAt );

        return messageResponse;
    }

    private Long messageSessionId(Message message) {
        if ( message == null ) {
            return null;
        }
        Session session = message.getSession();
        if ( session == null ) {
            return null;
        }
        Long id = session.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
