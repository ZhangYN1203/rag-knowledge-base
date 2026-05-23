package com.example.app.service;

import com.example.app.config.JwtUtil;
import com.example.app.dto.request.UserLoginRequest;
import com.example.app.dto.request.UserRegisterRequest;
import com.example.app.dto.response.LoginResponse;
import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil, authenticationManager);
    }

    @Test
    void register_shouldSucceed() {
        UserRegisterRequest request = new UserRegisterRequest("testuser", "password123", "test@example.com");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(jwtUtil.generateToken("testuser")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("testuser")).thenReturn("refresh-token");

        User savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded")
                .email("test@example.com")
                .role("USER")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        LoginResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        assertThat(response.getUser().getRole()).isEqualTo("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldFail_whenUsernameExists() {
        UserRegisterRequest request = new UserRegisterRequest("existing", "password123", "email@test.com");
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户名已存在");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldFail_whenEmailExists() {
        UserRegisterRequest request = new UserRegisterRequest("newuser", "password123", "used@test.com");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("used@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("邮箱已被使用");
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldSucceed() {
        UserLoginRequest request = new UserLoginRequest("testuser", "password123");
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded")
                .email("test@example.com")
                .role("USER")
                .enabled(true)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("testuser")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("testuser")).thenReturn("refresh-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_shouldFail_whenUserDisabled() {
        UserLoginRequest request = new UserLoginRequest("disabled", "password123");
        User user = User.builder()
                .username("disabled")
                .password("encoded")
                .enabled(false)
                .build();

        when(userRepository.findByUsername("disabled")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("账户已被禁用");
    }

    @Test
    void login_shouldFail_whenUserNotFound() {
        UserLoginRequest request = new UserLoginRequest("ghost", "password123");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    void refreshToken_shouldSucceed() {
        when(jwtUtil.extractUsername("valid-refresh-token")).thenReturn("testuser");
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded")
                .email("test@example.com")
                .role("USER")
                .enabled(true)
                .build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("testuser")).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken("testuser")).thenReturn("new-refresh-token");

        LoginResponse response = authService.refreshToken("valid-refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    void refreshToken_shouldFail_whenUserNotFound() {
        when(jwtUtil.extractUsername("bad-token")).thenReturn("ghost");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken("bad-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");
    }
}