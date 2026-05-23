package com.example.app.controller;

import com.example.app.dto.request.ChatRequest;
import com.example.app.dto.response.ApiResponse;
import com.example.app.dto.response.ChatResponse;
import com.example.app.dto.response.ConversationSummary;
import com.example.app.entity.ChatMessage;
import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import com.example.app.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    public ChatController(ChatService chatService, UserRepository userRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails user) {

        Long userId = getUserId(user);
        ChatResponse response = chatService.chat(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "回答成功"));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestParam String message,
            @RequestParam(required = false) String conversationId,
            @AuthenticationPrincipal UserDetails user) {

        Long userId = getUserId(user);
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        request.setConversationId(conversationId);

        return chatService.chatStreamSse(request, userId);
    }

    @GetMapping("/history/{conversationId}")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getHistory(
            @PathVariable String conversationId,
            @AuthenticationPrincipal UserDetails user) {

        List<ChatMessage> messages = chatService.getConversationHistory(conversationId);
        return ResponseEntity.ok(ApiResponse.success(messages, "获取成功"));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationSummary>>> getConversations(
            @AuthenticationPrincipal UserDetails user) {

        Long userId = getUserId(user);
        List<ConversationSummary> conversations = chatService.getUserConversations(userId);
        return ResponseEntity.ok(ApiResponse.success(conversations, "获取成功"));
    }

    @DeleteMapping("/history/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @PathVariable String conversationId,
            @AuthenticationPrincipal UserDetails user) {

        Long userId = getUserId(user);
        chatService.deleteConversation(conversationId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "删除成功"));
    }
}