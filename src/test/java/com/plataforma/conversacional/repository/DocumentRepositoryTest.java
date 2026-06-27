package com.plataforma.conversacional.repository;

import com.plataforma.conversacional.entity.Document;
import com.plataforma.conversacional.enums.DocumentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    private Document createDocument() {
        Document document = new Document();
        document.setOriginalName("test.pdf");
        document.setStorageFileName("uuid-test.pdf");
        document.setStoragePath("/uploads/uuid-test.pdf");
        document.setType(DocumentType.PDF);
        document.setSize(1024L);
        document.setContentType("application/pdf");
        return documentRepository.save(document);
    }

    @Test
    void save_ShouldPersistDocument() {
        Document document = createDocument();

        assertNotNull(document.getId());
        assertEquals("test.pdf", document.getOriginalName());
        assertEquals(DocumentType.PDF, document.getType());
        assertEquals(1024L, document.getSize());
        assertNotNull(document.getUploadedAt());
    }

    @Test
    void findById_ShouldReturnDocument_WhenExists() {
        Document document = createDocument();

        Optional<Document> found = documentRepository.findById(document.getId());

        assertTrue(found.isPresent());
        assertEquals(document.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Optional<Document> found = documentRepository.findById(999L);

        assertTrue(found.isEmpty());
    }
}
