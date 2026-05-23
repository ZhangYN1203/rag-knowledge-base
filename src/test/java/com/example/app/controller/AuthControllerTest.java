package com.example.app.controller;

import com.example.app.config.JwtUtil;
import com.example.app.dto.request.UserLoginRequest;
import com.example.app.dto.request.UserRegisterRequest;
import com.example.app.dto.response.ApiResponse;
import com.example.app.dto.response.LoginResponse;
import com.example.app.dto.response.UserResponse;
import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import com.example.app.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    void register_shouldReturn200() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("newuser", "password123", "new@test.com");
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("token")
                .refreshToken("refresh")
                .user(UserResponse.builder().id(1L).username("newuser").role("USER").build())
                .expiresIn(86400000L)
                .build();

        when(authService.register(any(UserRegisterRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("token"))
                .andExpect(jsonPath("$.data.user.username").value("newuser"));
    }

    @Test
    void login_shouldReturn200() throws Exception {
        UserLoginRequest request = new UserLoginRequest("testuser", "password123");
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("token")
                .refreshToken("refresh")
                .user(UserResponse.builder().id(1L).username("testuser").role("USER").build())
                .expiresIn(86400000L)
                .build();

        when(authService.login(any(UserLoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"));
    }

    @Test
    void refreshToken_shouldReturn200() throws Exception {
        when(authService.refreshToken("valid-refresh-token")).thenReturn(
                LoginResponse.builder().accessToken("new-token").refreshToken("new-refresh").build());

        mockMvc.perform(post("/api/auth/refresh")
                        .param("refreshToken", "valid-refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}