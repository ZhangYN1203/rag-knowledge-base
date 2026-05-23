package com.example.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false, length = 50)
    private String conversationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 10)
    private String role = "user";

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "reference_documents", columnDefinition = "TEXT")
    private String referenceDocuments;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public ChatMessage() {}

    public ChatMessage(Long id, String conversationId, Long userId, String role, String content, String referenceDocuments, LocalDateTime createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.userId = userId;
        this.role = role;
        this.content = content;
        this.referenceDocuments = referenceDocuments;
        this.createdAt = createdAt;
    }

    public static ChatMessageBuilder builder() {
        return new ChatMessageBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getReferenceDocuments() { return referenceDocuments; }
    public void setReferenceDocuments(String referenceDocuments) { this.referenceDocuments = referenceDocuments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class ChatMessageBuilder {
        private Long id;
        private String conversationId;
        private Long userId;
        private String role = "user";
        private String content;
        private String referenceDocuments;
        private LocalDateTime createdAt = LocalDateTime.now();

        public ChatMessageBuilder id(Long id) { this.id = id; return this; }
        public ChatMessageBuilder conversationId(String conversationId) { this.conversationId = conversationId; return this; }
        public ChatMessageBuilder userId(Long userId) { this.userId = userId; return this; }
        public ChatMessageBuilder role(String role) { this.role = role; return this; }
        public ChatMessageBuilder content(String content) { this.content = content; return this; }
        public ChatMessageBuilder referenceDocuments(String referenceDocuments) { this.referenceDocuments = referenceDocuments; return this; }
        public ChatMessageBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ChatMessage build() {
            return new ChatMessage(id, conversationId, userId, role, content, referenceDocuments, createdAt);
        }
    }
}
