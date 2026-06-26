package com.plataforma.conversacional.event;

import org.springframework.context.ApplicationEvent;

public class MessageSentEvent extends ApplicationEvent {

    private final Long messageId;

    public MessageSentEvent(Object source, Long messageId) {
        super(source);
        this.messageId = messageId;
    }

    public Long getMessageId() { return messageId; }
}