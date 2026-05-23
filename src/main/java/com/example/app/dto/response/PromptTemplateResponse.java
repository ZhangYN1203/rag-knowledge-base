package com.example.app.dto.response;

import java.time.LocalDateTime;

public class PromptTemplateResponse {

    private Long id;
    private String name;
    private String category;
    private String content;
    private String description;
    private String variables;
    private Integer usageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PromptTemplateResponse() {}

    public static PromptTemplateResponseBuilder builder() {
        return new PromptTemplateResponseBuilder();
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

    public static class PromptTemplateResponseBuilder {
        private Long id;
        private String name;
        private String category;
        private String content;
        private String description;
        private String variables;
        private Integer usageCount = 0;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public PromptTemplateResponseBuilder id(Long id) { this.id = id; return this; }
        public PromptTemplateResponseBuilder name(String name) { this.name = name; return this; }
        public PromptTemplateResponseBuilder category(String category) { this.category = category; return this; }
        public PromptTemplateResponseBuilder content(String content) { this.content = content; return this; }
        public PromptTemplateResponseBuilder description(String description) { this.description = description; return this; }
        public PromptTemplateResponseBuilder variables(String variables) { this.variables = variables; return this; }
        public PromptTemplateResponseBuilder usageCount(Integer usageCount) { this.usageCount = usageCount; return this; }
        public PromptTemplateResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PromptTemplateResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public PromptTemplateResponse build() {
            PromptTemplateResponse r = new PromptTemplateResponse();
            r.id = this.id;
            r.name = this.name;
            r.category = this.category;
            r.content = this.content;
            r.description = this.description;
            r.variables = this.variables;
            r.usageCount = this.usageCount;
            r.createdAt = this.createdAt;
            r.updatedAt = this.updatedAt;
            return r;
        }
    }
}