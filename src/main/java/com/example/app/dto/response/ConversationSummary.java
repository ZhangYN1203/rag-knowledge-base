package com.example.app.dto.response;

import java.time.LocalDateTime;

public class ConversationSummary {

    private String conversationId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private Integer messageCount;

    public ConversationSummary() {}

    public static ConversationSummaryBuilder builder() {
        return new ConversationSummaryBuilder();
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public Integer getMessageCount() { return messageCount; }
    public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }

    public static class ConversationSummaryBuilder {
        private String conversationId;
        private String title;
        private LocalDateTime createdAt;
        private LocalDateTime lastMessageAt;
        private Integer messageCount;

        public ConversationSummaryBuilder conversationId(String conversationId) { this.conversationId = conversationId; return this; }
        public ConversationSummaryBuilder title(String title) { this.title = title; return this; }
        public ConversationSummaryBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ConversationSummaryBuilder lastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; return this; }
        public ConversationSummaryBuilder messageCount(Integer messageCount) { this.messageCount = messageCount; return this; }

        public ConversationSummary build() {
            ConversationSummary r = new ConversationSummary();
            r.conversationId = this.conversationId;
            r.title = this.title;
            r.createdAt = this.createdAt;
            r.lastMessageAt = this.lastMessageAt;
            r.messageCount = this.messageCount;
            return r;
        }
    }
}