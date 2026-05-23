package com.example.app.service;

import com.example.app.dto.request.PromptTemplateRequest;
import com.example.app.dto.response.PromptTemplateResponse;
import com.example.app.entity.PromptTemplate;
import com.example.app.repository.PromptTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    private final PromptTemplateRepository repository;

    public TemplateService(PromptTemplateRepository repository) {
        this.repository = repository;
    }

    public List<PromptTemplateResponse> list(String category) {
        List<PromptTemplate> templates;
        if (category != null && !category.isBlank()) {
            templates = repository.findByCategory(category);
        } else {
            templates = repository.findAll();
        }
        return templates.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public PromptTemplateResponse getById(Long id) {
        PromptTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在"));
        return toResponse(template);
    }

    public PromptTemplateResponse getByName(String name) {
        PromptTemplate template = repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("模板不存在: " + name));
        return toResponse(template);
    }

    @Transactional
    public PromptTemplateResponse create(PromptTemplateRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new RuntimeException("模板名称已存在");
        }

        PromptTemplate template = PromptTemplate.builder()
                .name(request.getName())
                .category(request.getCategory() != null ? request.getCategory() : "default")
                .content(request.getContent())
                .description(request.getDescription())
                .variables(request.getVariables() != null ? request.getVariables() : extractVariablesAsJson(request.getContent()))
                .usageCount(0)
                .build();

        repository.save(template);
        return toResponse(template);
    }

    @Transactional
    public PromptTemplateResponse update(Long id, PromptTemplateRequest request) {
        PromptTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在"));

        template.setName(request.getName());
        template.setCategory(request.getCategory() != null ? request.getCategory() : "default");
        template.setContent(request.getContent());
        template.setDescription(request.getDescription());
        template.setVariables(request.getVariables() != null ? request.getVariables() : extractVariablesAsJson(request.getContent()));
        template.setUpdatedAt(LocalDateTime.now());

        repository.save(template);
        return toResponse(template);
    }

    @Transactional
    public void delete(Long id) {
        PromptTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在"));
        repository.delete(template);
    }

    /**
     * Render a template by name, replacing {{variable}} placeholders with actual values.
     */
    public String renderTemplateByName(String name, Map<String, String> variables) {
        PromptTemplate template = repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("模板不存在: " + name));

        // Increment usage count
        template.setUsageCount(template.getUsageCount() + 1);
        repository.save(template);

        return renderContent(template.getContent(), variables);
    }

    /**
     * Render a template by ID, replacing {{variable}} placeholders with actual values.
     */
    public String renderTemplate(Long id, Map<String, String> variables) {
        PromptTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在"));

        template.setUsageCount(template.getUsageCount() + 1);
        repository.save(template);

        return renderContent(template.getContent(), variables);
    }

    /**
     * Render raw template content with variables.
     */
    public String renderContent(String content, Map<String, String> variables) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(content, lastEnd, matcher.start());
            String varName = matcher.group(1);
            String value = variables.getOrDefault(varName, "{{" + varName + "}}");
            result.append(value);
            lastEnd = matcher.end();
        }
        result.append(content.substring(lastEnd));

        return result.toString();
    }

    /**
     * Extract all {{variable}} names from template content as JSON array string.
     */
    public String extractVariablesAsJson(String content) {
        List<String> vars = extractVariables(content);
        return "[" + vars.stream()
                .map(v -> "\"" + v + "\"")
                .collect(Collectors.joining(", ")) + "]";
    }

    /**
     * Extract all {{variable}} names from template content.
     */
    public List<String> extractVariables(String content) {
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        return matcher.results()
                .map(m -> m.group(1))
                .distinct()
                .collect(Collectors.toList());
    }

    private PromptTemplateResponse toResponse(PromptTemplate template) {
        return PromptTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .category(template.getCategory())
                .content(template.getContent())
                .description(template.getDescription())
                .variables(template.getVariables())
                .usageCount(template.getUsageCount())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}