package com.example.app.controller;

import com.example.app.dto.response.ApiResponse;
import com.example.app.dto.response.DocumentResponse;
import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import com.example.app.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    public DocumentController(DocumentService documentService, UserRepository userRepository) {
        this.documentService = documentService;
        this.userRepository = userRepository;
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "default") String category,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);

        try {
            DocumentResponse response = documentService.uploadDocument(file, category, userId);
            return ResponseEntity.ok(ApiResponse.success(response, "文档上传成功"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "上传失败：" + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getMyDocuments(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        List<DocumentResponse> documents = documentService.getUserDocuments(userId);
        return ResponseEntity.ok(ApiResponse.success(documents, "获取成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        documentService.deleteDocument(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "删除成功"));
    }
}