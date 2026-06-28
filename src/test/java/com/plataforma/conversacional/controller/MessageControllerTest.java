package com.plataforma.conversacional.controller;

import com.plataforma.conversacional.dto.request.SendMessageRequest;
import com.plataforma.conversacional.dto.response.MessageResponse;
import com.plataforma.conversacional.dto.response.SessionHistoryResponse;
import com.plataforma.conversacional.enums.MessageRole;
import com.plataforma.conversacional.enums.MessageStatus;
import com.plataforma.conversacional.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@WithMockUser
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @Test
    void send_ShouldReturn201() throws Exception {
        MessageResponse response = new MessageResponse(
                2L, 1L, "Resposta", MessageRole.ASSISTANT,
                MessageStatus.RECEIVED, "2024-01-01T00:00:00", "2024-01-01T00:00:00", null);
        when(messageService.send(anyLong(), any(SendMessageRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/sessions/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hello\"}")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.role").value("ASSISTANT"));
    }

    @Test
    void send_ShouldReturn400_WhenContentBlank() throws Exception {
        mockMvc.perform(post("/api/v1/sessions/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getHistory_ShouldReturn200() throws Exception {
        SessionHistoryResponse history = new SessionHistoryResponse(
                1L, List.of(), 0, 0, 0, false);
        when(messageService.getHistory(anyLong(), anyInt(), anyInt())).thenReturn(history);

        mockMvc.perform(get("/api/v1/sessions/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(1L))
                .andExpect(jsonPath("$.messages").isArray());
    }
}
