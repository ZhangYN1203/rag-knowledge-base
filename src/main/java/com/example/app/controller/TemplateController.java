package com.example.app.controller;

import com.example.app.dto.request.PromptTemplateRequest;
import com.example.app.dto.response.ApiResponse;
import com.example.app.dto.response.PromptTemplateResponse;
import com.example.app.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PromptTemplateResponse>>> list(
            @RequestParam(required = false) String category) {
        List<PromptTemplateResponse> templates = templateService.list(category);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromptTemplateResponse>> getById(@PathVariable Long id) {
        PromptTemplateResponse template = templateService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(template));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PromptTemplateResponse>> create(
            @Valid @RequestBody PromptTemplateRequest request) {
        PromptTemplateResponse template = templateService.create(request);
        return ResponseEntity.ok(ApiResponse.success(template, "创建成功"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromptTemplateResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PromptTemplateRequest request) {
        PromptTemplateResponse template = templateService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(template, "更新成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "删除成功"));
    }

    @PostMapping("/render/{id}")
    public ResponseEntity<ApiResponse<String>> render(
            @PathVariable Long id,
            @RequestBody Map<String, String> variables) {
        String rendered = templateService.renderTemplate(id, variables);
        return ResponseEntity.ok(ApiResponse.success(rendered, "渲染成功"));
    }
}