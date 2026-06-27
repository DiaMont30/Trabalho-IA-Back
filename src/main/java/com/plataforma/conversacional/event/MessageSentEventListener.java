package com.plataforma.conversacional.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MessageSentEventListener {

    private static final Logger log = LoggerFactory.getLogger(MessageSentEventListener.class);

    @EventListener
    public void handleMessageSent(MessageSentEvent event) {
        log.info("MessageSentEvent received for messageId={}, messageType={}", event.getMessageId(), event.getMessageType());
    }
}
