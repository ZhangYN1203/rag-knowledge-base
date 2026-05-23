package com.example.app.controller;

import com.example.app.config.JwtUtil;
import com.example.app.dto.request.PromptTemplateRequest;
import com.example.app.dto.response.PromptTemplateResponse;
import com.example.app.repository.UserRepository;
import com.example.app.service.TemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TemplateService templateService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser
    void list_shouldReturn200() throws Exception {
        when(templateService.list(null)).thenReturn(List.of(
                PromptTemplateResponse.builder().id(1L).name("t1").content("c1").build()
        ));

        mockMvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("t1"));
    }

    @Test
    @WithMockUser
    void list_shouldFilterByCategory() throws Exception {
        when(templateService.list("rag")).thenReturn(List.of(
                PromptTemplateResponse.builder().id(1L).name("rag-template").content("c").build()
        ));

        mockMvc.perform(get("/api/templates").param("category", "rag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("rag-template"));
    }

    @Test
    @WithMockUser
    void getById_shouldReturn200() throws Exception {
        when(templateService.getById(1L)).thenReturn(
                PromptTemplateResponse.builder().id(1L).name("test").content("Hello").build());

        mockMvc.perform(get("/api/templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("test"));
    }

    @Test
    @WithMockUser
    void create_shouldReturn200() throws Exception {
        PromptTemplateRequest request = new PromptTemplateRequest();
        request.setName("new-template");
        request.setContent("Content with {{var}}");

        when(templateService.create(any(PromptTemplateRequest.class))).thenReturn(
                PromptTemplateResponse.builder().id(1L).name("new-template").content("Content with {{var}}").build());

        mockMvc.perform(post("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("new-template"));
    }

    @Test
    @WithMockUser
    void update_shouldReturn200() throws Exception {
        PromptTemplateRequest request = new PromptTemplateRequest();
        request.setName("updated");
        request.setContent("Updated content");

        when(templateService.update(any(), any(PromptTemplateRequest.class))).thenReturn(
                PromptTemplateResponse.builder().id(1L).name("updated").content("Updated content").build());

        mockMvc.perform(put("/api/templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser
    void delete_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser
    void render_shouldReturn200() throws Exception {
        when(templateService.renderTemplate(any(), any())).thenReturn("Hello World");

        mockMvc.perform(post("/api/templates/render/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"World\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Hello World"));
    }
}