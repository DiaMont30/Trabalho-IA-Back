package com.plataforma.conversacional.repository;

import com.plataforma.conversacional.entity.Session;
import com.plataforma.conversacional.enums.SessionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SessionRepositoryTest {

    @Autowired
    private SessionRepository sessionRepository;

    @Test
    void save_ShouldPersistSession() {
        Session session = new Session();
        session.setStatus(SessionStatus.ACTIVE);
        session.setTitle("Test session");

        Session saved = sessionRepository.save(session);

        assertNotNull(saved.getId());
        assertEquals(SessionStatus.ACTIVE, saved.getStatus());
        assertEquals("Test session", saved.getTitle());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findById_ShouldReturnSession_WhenExists() {
        Session session = new Session();
        session.setStatus(SessionStatus.ACTIVE);
        session = sessionRepository.save(session);

        Optional<Session> found = sessionRepository.findById(session.getId());

        assertTrue(found.isPresent());
        assertEquals(session.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Optional<Session> found = sessionRepository.findById(999L);

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnPagedResults() {
        for (int i = 0; i < 5; i++) {
            Session session = new Session();
            session.setStatus(SessionStatus.ACTIVE);
            sessionRepository.save(session);
        }

        Page<Session> page = sessionRepository.findAll(PageRequest.of(0, 3));

        assertEquals(3, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void findAll_ShouldReturnEmptyPage_WhenNoSessions() {
        Page<Session> page = sessionRepository.findAll(PageRequest.of(0, 10));

        assertTrue(page.getContent().isEmpty());
        assertEquals(0, page.getTotalElements());
    }
}
