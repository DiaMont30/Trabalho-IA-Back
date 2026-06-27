package com.plataforma.conversacional.event;

import org.springframework.context.ApplicationEvent;

public class MessageSentEvent extends ApplicationEvent {

    private final Long messageId;
    private final String messageType;

    public MessageSentEvent(Object source, Long messageId, String messageType) {
        super(source);
        this.messageId = messageId;
        this.messageType = messageType;
    }

    public Long getMessageId() { return messageId; }

    public String getMessageType() { return messageType; }
}