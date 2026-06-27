package com.plataforma.conversacional.controller;

import com.plataforma.conversacional.dto.response.SessionPageResponse;
import com.plataforma.conversacional.dto.response.SessionResponse;
import com.plataforma.conversacional.enums.SessionStatus;
import com.plataforma.conversacional.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionController.class)
@WithMockUser
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionService sessionService;

    @Test
    void create_ShouldReturn201() throws Exception {
        SessionResponse response = new SessionResponse(
                1L, "Nova conversa", SessionStatus.ACTIVE,
                "2024-01-01T00:00:00", "2024-01-01T00:00:00");
        when(sessionService.create()).thenReturn(response);

        mockMvc.perform(post("/api/v1/sessions").with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void findById_ShouldReturn200() throws Exception {
        SessionResponse response = new SessionResponse(
                1L, "Test", SessionStatus.ACTIVE,
                "2024-01-01T00:00:00", "2024-01-01T00:00:00");
        when(sessionService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void listAll_ShouldReturn200() throws Exception {
        SessionPageResponse pageResponse = new SessionPageResponse(
                List.of(), 0, 0, 0, false);
        when(sessionService.findAll(anyInt(), anyInt())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessions").isArray());
    }
}
