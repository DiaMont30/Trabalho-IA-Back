package com.plataforma.conversacional.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MessageEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public MessageEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishMessageSent(Long messageId) {
        eventPublisher.publishEvent(new MessageSentEvent(this, messageId));
    }
}