package com.plataforma.conversacional.repository;

import com.plataforma.conversacional.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
}