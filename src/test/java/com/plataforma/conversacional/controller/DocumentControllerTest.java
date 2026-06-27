package com.plataforma.conversacional.controller;

import com.plataforma.conversacional.dto.internal.FileUploadData;
import com.plataforma.conversacional.dto.response.DocumentResponse;
import com.plataforma.conversacional.enums.DocumentType;
import com.plataforma.conversacional.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@WithMockUser
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    void upload_ShouldReturn201() throws Exception {
        DocumentResponse response = new DocumentResponse(
                1L, "uuid.pdf", "test.pdf", DocumentType.PDF,
                1024L, "/uploads/uuid.pdf", null, "2024-01-01T00:00:00");
        when(documentService.store(any(FileUploadData.class))).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("PDF"));
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        DocumentResponse response = new DocumentResponse(
                1L, "uuid.pdf", "test.pdf", DocumentType.PDF,
                1024L, "/uploads/uuid.pdf", null, "2024-01-01T00:00:00");
        when(documentService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.originalName").value("test.pdf"));
    }
}
