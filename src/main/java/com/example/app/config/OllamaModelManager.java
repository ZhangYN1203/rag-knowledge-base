package com.example.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@Order(1)
@Profile("!openai")
public class OllamaModelManager implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OllamaModelManager.class);

    private static final int MAX_RETRIES = 30;
    private static final long RETRY_DELAY_MS = 5000L;
    private static final long PULL_TIMEOUT_MS = 600_000L; // 10 minutes max per model

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(String... args) {
        waitForOllamaReady();
        pullModel("nomic-embed-text");
        pullModel("qwen2:0.5b");
        // nomic-embed-text is an embedding model — it doesn't support /api/generate, skip warmup
        warmupModel("qwen2:0.5b");
        log.info("All AI models are ready and loaded in memory.");
    }

    private void waitForOllamaReady() {
        String checkUrl = ollamaBaseUrl + "/api/tags";
        for (int i = 1; i <= MAX_RETRIES; i++) {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(checkUrl, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Ollama service is ready.");
                    return;
                }
            } catch (Exception e) {
                log.warn("Ollama not ready yet (attempt {}/{}): {}", i, MAX_RETRIES, e.getMessage());
            }
            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for Ollama");
                return;
            }
        }
        log.warn("Ollama service did not become ready after {} retries. Proceeding anyway.", MAX_RETRIES);
    }

    private void pullModel(String modelName) {
        log.info("Pulling model: {} ...", modelName);

        String pullUrl = ollamaBaseUrl + "/api/pull";
        String requestBody = "{\"name\": \"" + modelName + "\"}";
        byte[] requestBytes = requestBody.getBytes(StandardCharsets.UTF_8);

        try {
            restTemplate.execute(java.net.URI.create(pullUrl), HttpMethod.POST,
                request -> {
                    request.getBody().write(requestBytes);
                },
                response -> {
                    if (response.getStatusCode() != HttpStatus.OK) {
                        log.warn("Failed to pull model {}, HTTP {}", modelName, response.getStatusCode());
                        return null;
                    }

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {

                        String line;
                        long startTime = System.currentTimeMillis();
                        int lastPercent = -1;

                        while ((line = reader.readLine()) != null) {
                            if (System.currentTimeMillis() - startTime > PULL_TIMEOUT_MS) {
                                log.warn("Pulling model {} timed out after {}ms", modelName, PULL_TIMEOUT_MS);
                                break;
                            }
                            if (line.isEmpty()) continue;

                            // Parse status line
                            if (line.contains("\"status\"")) {
                                String status = extractJsonStringValue(line, "status");
                                if (status != null) {
                                    if (status.equals("success")) {
                                        log.info("Model {} pulled successfully.", modelName);
                                        break;
                                    } else if (status.startsWith("downloading")) {
                                        int percent = extractIntValue(line, "completed");
                                        if (percent >= 0 && percent != lastPercent) {
                                            if (percent % 20 == 0 || percent == 100) {
                                                log.info("  {}: downloading... {}%", modelName, percent);
                                            }
                                            lastPercent = percent;
                                        }
                                    } else {
                                        log.debug("  {}: {}", modelName, status);
                                    }
                                }
                            }
                        }
                    }
                    return null;
                });
        } catch (Exception e) {
            log.warn("Failed to pull model {}: {}", modelName, e.getMessage());
        }
    }

    private void warmupModel(String modelName) {
        log.info("Warming up model: {} ...", modelName);

        String generateUrl = ollamaBaseUrl + "/api/generate";
        // keep_alive=30m keeps the model loaded in memory for 30 minutes after last use
        String requestBody = "{\"model\":\"" + modelName + "\",\"prompt\":\"ping\",\"stream\":false,\"keep_alive\":\"30m\"}";
        byte[] requestBytes = requestBody.getBytes(StandardCharsets.UTF_8);

        try {
            restTemplate.execute(java.net.URI.create(generateUrl), HttpMethod.POST,
                request -> request.getBody().write(requestBytes),
                response -> {
                    if (response.getStatusCode() == HttpStatus.OK) {
                        log.info("Model {} warmed up and kept in memory (keep_alive=30m).", modelName);
                    } else {
                        log.warn("Warmup responded with HTTP {}", response.getStatusCode());
                    }
                    return null;
                });
        } catch (Exception e) {
            log.warn("Failed to warm up model {}: {}", modelName, e.getMessage());
        }
    }

    private String extractJsonStringValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        return end > start ? json.substring(start, end) : null;
    }

    private int extractIntValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) return -1;
        start += search.length();
        int end = json.indexOf(",", start);
        if (end < 0) end = json.indexOf("}", start);
        if (end < 0) return -1;
        try {
            String num = json.substring(start, end).trim();
            // Handle decimal by truncating
            int dot = num.indexOf('.');
            if (dot >= 0) num = num.substring(0, dot);
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}