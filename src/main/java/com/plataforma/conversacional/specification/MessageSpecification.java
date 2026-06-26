package com.plataforma.conversacional.specification;

import com.plataforma.conversacional.entity.Message;
import org.springframework.data.jpa.domain.Specification;

public class MessageSpecification {

    public static Specification<Message> bySessionId(Long sessionId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}