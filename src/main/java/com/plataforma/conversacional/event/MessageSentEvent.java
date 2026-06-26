package com.plataforma.conversacional.event;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

public class MessageSentEvent extends ApplicationEvent {

    private final UUID messageId;

    public MessageSentEvent(Object source, UUID messageId) {
        super(source);
        this.messageId = messageId;
    }

    public UUID getMessageId() { return messageId; }
}
