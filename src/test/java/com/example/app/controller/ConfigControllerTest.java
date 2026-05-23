package com.example.app.controller;

import com.example.app.config.JwtUtil;
import com.example.app.dto.request.AiConfigRequest;
import com.example.app.dto.response.AiConfigResponse;
import com.example.app.config.ModelProvider;
import com.example.app.repository.UserRepository;
import com.example.app.service.ConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConfigService configService;

    @MockBean
    private ModelProvider modelProvider;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser
    void getAiConfig_shouldReturn200() throws Exception {
        when(configService.get("ai.provider", "ollama")).thenReturn("ollama");
        when(configService.get("ai.chat.model", "")).thenReturn("qwen2:7b");
        when(configService.get("ai.embedding.model", "")).thenReturn("nomic-embed-text");
        when(configService.get("ai.base-url", "")).thenReturn("http://localhost:11434");
        when(configService.get("ai.temperature", "0.7")).thenReturn("0.7");
        when(configService.get("ai.max-tokens", "2048")).thenReturn("2048");

        mockMvc.perform(get("/api/config/ai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.provider").value("ollama"))
                .andExpect(jsonPath("$.data.chatModel").value("qwen2:7b"))
                .andExpect(jsonPath("$.data.temperature").value(0.7))
                .andExpect(jsonPath("$.data.maxTokens").value(2048));
    }

    @Test
    @WithMockUser
    void updateAiConfig_shouldReturn200() throws Exception {
        AiConfigRequest request = new AiConfigRequest();
        request.setProvider("ollama");
        request.setChatModel("qwen2:7b");
        request.setTemperature(0.8);
        request.setMaxTokens(4096);

        mockMvc.perform(put("/api/config/ai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}