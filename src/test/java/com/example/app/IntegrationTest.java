package com.example.app;

import com.example.app.entity.ChatMessage;
import com.example.app.entity.User;
import com.example.app.repository.ChatMessageRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.AuthService;
import com.example.app.service.TemplateService;
import com.example.app.dto.request.UserRegisterRequest;
import com.example.app.dto.request.PromptTemplateRequest;
import com.example.app.dto.response.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        UserRegisterRequest registerRequest = new UserRegisterRequest("integration_user", "password123", "integration@test.com");
        LoginResponse loginResponse = authService.register(registerRequest);
        userId = loginResponse.getUser().getId();
    }

    @Test
    void fullAuthFlow_registerAndLogin() {
        // Register was done in setUp, verify user exists
        User user = userRepository.findByUsername("integration_user").orElseThrow();
        assertThat(user.getEmail()).isEqualTo("integration@test.com");
        assertThat(user.getRole()).isEqualTo("USER");
        assertThat(user.getEnabled()).isTrue();

        // Login
        var loginResponse = authService.login(
                new com.example.app.dto.request.UserLoginRequest("integration_user", "password123"));
        assertThat(loginResponse.getAccessToken()).isNotNull();
        assertThat(loginResponse.getUser().getUsername()).isEqualTo("integration_user");

        // Refresh token
        var refreshResponse = authService.refreshToken(loginResponse.getRefreshToken());
        assertThat(refreshResponse.getAccessToken()).isNotNull();
    }

    @Test
    void register_shouldFailWithDuplicateUsername() {
        UserRegisterRequest dupRequest = new UserRegisterRequest("integration_user", "other123", "other@test.com");
        assertThatThrownBy(() -> authService.register(dupRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    void templateCrudFlow() {
        PromptTemplateRequest request = new PromptTemplateRequest();
        request.setName("test-template");
        request.setContent("Answer based on {{context}}");
        request.setCategory("qa");

        var created = templateService.create(request);
        assertThat(created.getName()).isEqualTo("test-template");

        var found = templateService.getById(created.getId());
        assertThat(found.getContent()).isEqualTo("Answer based on {{context}}");

        PromptTemplateRequest updateReq = new PromptTemplateRequest();
        updateReq.setName("test-template");
        updateReq.setContent("Updated: {{context}}");
        updateReq.setCategory("qa");

        var updated = templateService.update(created.getId(), updateReq);
        assertThat(updated.getContent()).isEqualTo("Updated: {{context}}");

        var rendered = templateService.renderTemplate(created.getId(),
                java.util.Map.of("context", "Hello World"));
        assertThat(rendered).isEqualTo("Updated: Hello World");

        templateService.delete(created.getId());
        assertThatThrownBy(() -> templateService.getById(created.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void renderTemplateByName_shouldWork() {
        PromptTemplateRequest request = new PromptTemplateRequest();
        request.setName("greeting");
        request.setContent("Hello {{name}}!");

        templateService.create(request);

        String result = templateService.renderTemplateByName("greeting", java.util.Map.of("name", "World"));
        assertThat(result).isEqualTo("Hello World!");
    }

    @Test
    void templateWithoutCategory_shouldDefault() {
        PromptTemplateRequest request = new PromptTemplateRequest();
        request.setName("no-cat");
        request.setContent("Some content");

        var created = templateService.create(request);
        assertThat(created.getCategory()).isEqualTo("default");
    }
}