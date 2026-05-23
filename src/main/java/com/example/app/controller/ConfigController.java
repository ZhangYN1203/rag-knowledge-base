package com.example.app.controller;

import com.example.app.dto.request.AiConfigRequest;
import com.example.app.dto.response.AiConfigResponse;
import com.example.app.dto.response.ApiResponse;
import com.example.app.service.ConfigService;
import com.example.app.config.ModelProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigService configService;
    private final ModelProvider modelProvider;

    public ConfigController(ConfigService configService, ModelProvider modelProvider) {
        this.configService = configService;
        this.modelProvider = modelProvider;
    }

    @GetMapping("/ai")
    public ResponseEntity<ApiResponse<AiConfigResponse>> getAiConfig() {
        AiConfigResponse response = AiConfigResponse.builder()
                .provider(configService.get("ai.provider", "ollama"))
                .chatModel(configService.get("ai.chat.model", ""))
                .embeddingModel(configService.get("ai.embedding.model", ""))
                .baseUrl(configService.get("ai.base-url", ""))
                .temperature(Double.valueOf(configService.get("ai.temperature", "0.7")))
                .maxTokens(Integer.valueOf(configService.get("ai.max-tokens", "2048")))
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/ai")
    public ResponseEntity<ApiResponse<Void>> updateAiConfig(@Valid @RequestBody AiConfigRequest request) {
        configService.set("ai.provider", request.getProvider());
        if (request.getChatModel() != null) configService.set("ai.chat.model", request.getChatModel());
        if (request.getEmbeddingModel() != null) configService.set("ai.embedding.model", request.getEmbeddingModel());
        if (request.getBaseUrl() != null) configService.set("ai.base-url", request.getBaseUrl());
        if (request.getTemperature() != null) configService.set("ai.temperature", String.valueOf(request.getTemperature()));
        if (request.getMaxTokens() != null) configService.set("ai.max-tokens", String.valueOf(request.getMaxTokens()));
        if ("openai".equals(request.getProvider()) && request.getApiKey() != null) {
            configService.set("ai.openai.api-key", request.getApiKey());
        }

        modelProvider.refreshModel();
        return ResponseEntity.ok(ApiResponse.success(null, "AI configuration updated"));
    }
}