package com.example.app.service;

import com.example.app.entity.ConfigProperty;
import com.example.app.repository.ConfigPropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConfigService {

    private final ConfigPropertyRepository repository;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public ConfigService(ConfigPropertyRepository repository) {
        this.repository = repository;
        loadAll();
    }

    public String get(String key, String defaultValue) {
        return cache.getOrDefault(key, defaultValue);
    }

    public void set(String key, String value) {
        ConfigProperty prop = repository.findById(key)
                .orElse(new ConfigProperty(key, value));
        prop.setConfigValue(value);
        repository.save(prop);
        cache.put(key, value);
    }

    public Map<String, String> getAllByPrefix(String prefix) {
        Map<String, String> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, String> entry : cache.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public void loadAll() {
        List<ConfigProperty> all = repository.findAll();
        for (ConfigProperty prop : all) {
            cache.put(prop.getConfigKey(), prop.getConfigValue());
        }

        // Set defaults if empty
        setDefaultIfAbsent("ai.provider", "ollama");
        setDefaultIfAbsent("ai.chat.model", "qwen2:7b");
        setDefaultIfAbsent("ai.embedding.model", "nomic-embed-text");
        setDefaultIfAbsent("ai.base-url", "http://localhost:11434");
        setDefaultIfAbsent("ai.temperature", "0.7");
        setDefaultIfAbsent("ai.max-tokens", "2048");
    }

    private void setDefaultIfAbsent(String key, String value) {
        cache.putIfAbsent(key, value);
        if (repository.findById(key).isEmpty()) {
            repository.save(new ConfigProperty(key, value));
        }
    }
}