package com.example.app.dto.request;

import jakarta.validation.constraints.NotBlank;

public class PromptTemplateRequest {

    @NotBlank(message = "模板名称不能为空")
    private String name;

    private String category;

    @NotBlank(message = "模板内容不能为空")
    private String content;

    private String description;

    private String variables;

    public PromptTemplateRequest() {}

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
}