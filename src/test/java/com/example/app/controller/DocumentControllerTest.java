package com.example.app.controller;

import com.example.app.config.JwtUtil;
import com.example.app.dto.response.DocumentResponse;
import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import com.example.app.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(username = "testuser")
    void uploadDocument_shouldReturn200() throws Exception {
        User user = User.builder().id(1L).username("testuser").role("USER").enabled(true).build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(documentService.uploadDocument(any(), eq("general"), eq(1L)))
                .thenReturn(DocumentResponse.builder().id(1L).title("test.txt").processed(true).build());

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Hello World".getBytes());

        mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .param("category", "general"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("test.txt"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getMyDocuments_shouldReturn200() throws Exception {
        User user = User.builder().id(1L).username("testuser").role("USER").enabled(true).build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(documentService.getUserDocuments(1L)).thenReturn(List.of(
                DocumentResponse.builder().id(1L).title("doc1").build(),
                DocumentResponse.builder().id(2L).title("doc2").build()
        ));

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteDocument_shouldReturn200() throws Exception {
        User user = User.builder().id(1L).username("testuser").role("USER").enabled(true).build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));
    }
}