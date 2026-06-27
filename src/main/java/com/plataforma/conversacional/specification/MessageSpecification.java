package com.plataforma.conversacional.specification;

import com.plataforma.conversacional.entity.Message;
import org.springframework.data.jpa.domain.Specification;

public class MessageSpecification {

    public static Specification<Message> bySessionId(Long sessionId) {
        return (root, query, cb) -> {
            if (sessionId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("session").get("id"), sessionId);
        };
    }
}