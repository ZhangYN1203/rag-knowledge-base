package com.example.app.service;

import com.example.app.entity.ConfigProperty;
import com.example.app.repository.ConfigPropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {

    @Mock
    private ConfigPropertyRepository repository;

    @Mock
    private Environment environment;

    private ConfigService configService;

    @BeforeEach
    void setUp() {
        when(repository.findAll()).thenReturn(List.of());
        when(repository.save(any(ConfigProperty.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        configService = new ConfigService(repository, environment);
    }

    @Test
    void get_shouldReturnDefault_whenNotSet() {
        assertThat(configService.get("nonexistent", "default-value")).isEqualTo("default-value");
    }

    @Test
    void get_shouldReturnStoredValue() {
        configService.set("test.key", "stored-value");
        assertThat(configService.get("test.key", "default")).isEqualTo("stored-value");
    }

    @Test
    void set_shouldPersistAndCache() {
        configService.set("my.key", "my-value");
        verify(repository).save(any(ConfigProperty.class));
        assertThat(configService.get("my.key", "")).isEqualTo("my-value");
    }

    @Test
    void set_shouldUpdateExisting() {
        ConfigProperty existing = new ConfigProperty("existing.key", "old");
        when(repository.findById("existing.key")).thenReturn(Optional.of(existing));

        configService.set("existing.key", "new");

        assertThat(existing.getConfigValue()).isEqualTo("new");
        verify(repository).save(existing);
        assertThat(configService.get("existing.key", "")).isEqualTo("new");
    }

    @Test
    void getAllByPrefix_shouldReturnMatchingKeys() {
        configService.set("ai.provider", "ollama");
        configService.set("ai.chat.model", "qwen2:7b");
        configService.set("app.name", "test");

        Map<String, String> result = configService.getAllByPrefix("ai.");

        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("ai.provider", "ai.chat.model");
    }

    @Test
    void loadAll_shouldSetDefaults() {
        assertThat(configService.get("ai.provider", "")).isEqualTo("ollama");
        assertThat(configService.get("ai.chat.model", "")).isEqualTo("qwen2:7b");
        assertThat(configService.get("ai.embedding.model", "")).isEqualTo("nomic-embed-text");
        assertThat(configService.get("ai.base-url", "")).isEqualTo("http://localhost:11434");
        assertThat(configService.get("ai.temperature", "")).isEqualTo("0.7");
        assertThat(configService.get("ai.max-tokens", "")).isEqualTo("2048");
    }

    @Test
    void loadAll_shouldNotOverwriteExisting() {
        when(repository.findById("ai.provider")).thenReturn(Optional.of(new ConfigProperty("ai.provider", "existing")));
        configService = new ConfigService(repository, environment);

        assertThat(configService.get("ai.provider", "")).isEqualTo("existing");
    }
}