package com.plataforma.conversacional.repository;

import com.plataforma.conversacional.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
}
