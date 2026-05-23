package com.example.app.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponse {

    private String conversationId;
    private String answer;
    private List<ReferenceSource> sources;
    private String modelName;
    private TokenUsage tokenUsage;
    private LocalDateTime createdAt;

    public ChatResponse() {}

    public static ChatResponseBuilder builder() {
        return new ChatResponseBuilder();
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public List<ReferenceSource> getSources() { return sources; }
    public void setSources(List<ReferenceSource> sources) { this.sources = sources; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public TokenUsage getTokenUsage() { return tokenUsage; }
    public void setTokenUsage(TokenUsage tokenUsage) { this.tokenUsage = tokenUsage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class ReferenceSource {
        private String title;
        private String excerpt;
        private Double similarity;

        public ReferenceSource() {}

        public ReferenceSource(String title, String excerpt, Double similarity) {
            this.title = title;
            this.excerpt = excerpt;
            this.similarity = similarity;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getExcerpt() { return excerpt; }
        public void setExcerpt(String excerpt) { this.excerpt = excerpt; }

        public Double getSimilarity() { return similarity; }
        public void setSimilarity(Double similarity) { this.similarity = similarity; }
    }

    public static class TokenUsage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;

        public TokenUsage() {}

        public TokenUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
        }

        public Integer getPromptTokens() { return promptTokens; }
        public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }

        public Integer getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }

        public Integer getTotalTokens() { return totalTokens; }
        public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }
    }

    public static class ChatResponseBuilder {
        private String conversationId;
        private String answer;
        private List<ReferenceSource> sources;
        private String modelName;
        private TokenUsage tokenUsage;
        private LocalDateTime createdAt;

        public ChatResponseBuilder conversationId(String conversationId) { this.conversationId = conversationId; return this; }
        public ChatResponseBuilder answer(String answer) { this.answer = answer; return this; }
        public ChatResponseBuilder sources(List<ReferenceSource> sources) { this.sources = sources; return this; }
        public ChatResponseBuilder modelName(String modelName) { this.modelName = modelName; return this; }
        public ChatResponseBuilder tokenUsage(TokenUsage tokenUsage) { this.tokenUsage = tokenUsage; return this; }
        public ChatResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ChatResponse build() {
            ChatResponse r = new ChatResponse();
            r.conversationId = this.conversationId;
            r.answer = this.answer;
            r.sources = this.sources;
            r.modelName = this.modelName;
            r.tokenUsage = this.tokenUsage;
            r.createdAt = this.createdAt;
            return r;
        }
    }
}