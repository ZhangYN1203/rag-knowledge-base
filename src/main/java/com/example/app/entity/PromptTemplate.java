package com.example.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_templates")
public class PromptTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String category = "default";

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String variables;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public PromptTemplate() {}

    public PromptTemplate(Long id, String name, String category, String content, String description, String variables, Integer usageCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.content = content;
        this.description = description;
        this.variables = variables;
        this.usageCount = usageCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PromptTemplateBuilder builder() {
        return new PromptTemplateBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }

    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class PromptTemplateBuilder {
        private Long id;
        private String name;
        private String category = "default";
        private String content;
        private String description;
        private String variables;
        private Integer usageCount = 0;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public PromptTemplateBuilder id(Long id) { this.id = id; return this; }
        public PromptTemplateBuilder name(String name) { this.name = name; return this; }
        public PromptTemplateBuilder category(String category) { this.category = category; return this; }
        public PromptTemplateBuilder content(String content) { this.content = content; return this; }
        public PromptTemplateBuilder description(String description) { this.description = description; return this; }
        public PromptTemplateBuilder variables(String variables) { this.variables = variables; return this; }
        public PromptTemplateBuilder usageCount(Integer usageCount) { this.usageCount = usageCount; return this; }
        public PromptTemplateBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PromptTemplateBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public PromptTemplate build() {
            return new PromptTemplate(id, name, category, content, description, variables, usageCount, createdAt, updatedAt);
        }
    }
}
