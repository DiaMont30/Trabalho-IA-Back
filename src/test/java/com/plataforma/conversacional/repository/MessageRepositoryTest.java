package com.plataforma.conversacional.repository;

import com.plataforma.conversacional.entity.Message;
import com.plataforma.conversacional.entity.Session;
import com.plataforma.conversacional.enums.MessageRole;
import com.plataforma.conversacional.enums.MessageStatus;
import com.plataforma.conversacional.enums.SessionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SessionRepository sessionRepository;

    private Session createSession() {
        Session session = new Session();
        session.setStatus(SessionStatus.ACTIVE);
        return sessionRepository.save(session);
    }

    @Test
    void save_ShouldPersistMessage() {
        Session session = createSession();
        Message message = new Message();
        message.setSession(session);
        message.setContent("Hello");
        message.setRole(MessageRole.USER);
        message.setStatus(MessageStatus.SENT);

        Message saved = messageRepository.save(message);

        assertNotNull(saved.getId());
        assertEquals("Hello", saved.getContent());
        assertEquals(MessageRole.USER, saved.getRole());
        assertEquals(MessageStatus.SENT, saved.getStatus());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findBySessionId_ShouldReturnPagedMessages() {
        Session session = createSession();
        for (int i = 0; i < 4; i++) {
            Message message = new Message();
            message.setSession(session);
            message.setContent("Message " + i);
            message.setRole(i % 2 == 0 ? MessageRole.USER : MessageRole.ASSISTANT);
            message.setStatus(MessageStatus.SENT);
            messageRepository.save(message);
        }

        Page<Message> page = messageRepository.findBySessionId(session.getId(), PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(4, page.getTotalElements());
    }

    @Test
    void findBySessionId_ShouldReturnEmpty_WhenNoMessages() {
        Session session = createSession();

        Page<Message> page = messageRepository.findBySessionId(session.getId(), PageRequest.of(0, 10));

        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void findBySessionId_ShouldReturnEmpty_WhenSessionNotExists() {
        Page<Message> page = messageRepository.findBySessionId(999L, PageRequest.of(0, 10));

        assertTrue(page.getContent().isEmpty());
    }
}
