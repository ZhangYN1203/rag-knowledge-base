package com.example.app.controller;

import com.example.app.config.JwtUtil;
import com.example.app.dto.request.ChatRequest;
import com.example.app.dto.response.ApiResponse;
import com.example.app.dto.response.ChatResponse;
import com.example.app.dto.response.ConversationSummary;
import com.example.app.entity.ChatMessage;
import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import com.example.app.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(username = "testuser")
    void chat_shouldReturn200() throws Exception {
        User user = User.builder().id(1L).username("testuser").role("USER").enabled(true).build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        ChatRequest request = new ChatRequest("Hello", null);
        ChatResponse chatResponse = ChatResponse.builder()
                .conversationId("conv-1")
                .answer("Hi there!")
                .createdAt(LocalDateTime.now())
                .build();

        when(chatService.chat(any(ChatRequest.class), eq(1L))).thenReturn(chatResponse);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.answer").value("Hi there!"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getHistory_shouldReturn200() throws Exception {
        User user = User.builder().id(1L).username("testuser").role("USER").enabled(true).build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        when(chatService.getConversationHistory("conv-1")).thenReturn(List.of(
                ChatMessage.builder().conversationId("conv-1").role("user").content("Hi").build()
        ));

        mockMvc.perform(get("/api/chat/history/conv-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getConversations_shouldReturn200() throws Exception {
        User user = User.builder().id(1L).username("testuser").role("USER").enabled(true).build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(chatService.getUserConversations(1L)).thenReturn(List.of(
                ConversationSummary.builder().conversationId("conv-1").title("Hello").messageCount(2).build()
        ));

        mockMvc.perform(get("/api/chat/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteConversation_shouldReturn200() throws Exception {
        User user = User.builder().id(1L).username("testuser").role("USER").enabled(true).build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/chat/history/conv-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }
}