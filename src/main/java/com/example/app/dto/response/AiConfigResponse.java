package com.example.app.dto.response;

public class AiConfigResponse {

    private String provider;
    private String chatModel;
    private String embeddingModel;
    private String baseUrl;
    private Double temperature;
    private Integer maxTokens;

    public AiConfigResponse() {}

    public static AiConfigResponseBuilder builder() {
        return new AiConfigResponseBuilder();
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getChatModel() { return chatModel; }
    public void setChatModel(String chatModel) { this.chatModel = chatModel; }

    public String getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

    public static class AiConfigResponseBuilder {
        private String provider;
        private String chatModel;
        private String embeddingModel;
        private String baseUrl;
        private Double temperature;
        private Integer maxTokens;

        public AiConfigResponseBuilder provider(String provider) { this.provider = provider; return this; }
        public AiConfigResponseBuilder chatModel(String chatModel) { this.chatModel = chatModel; return this; }
        public AiConfigResponseBuilder embeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; return this; }
        public AiConfigResponseBuilder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }
        public AiConfigResponseBuilder temperature(Double temperature) { this.temperature = temperature; return this; }
        public AiConfigResponseBuilder maxTokens(Integer maxTokens) { this.maxTokens = maxTokens; return this; }

        public AiConfigResponse build() {
            AiConfigResponse r = new AiConfigResponse();
            r.provider = this.provider;
            r.chatModel = this.chatModel;
            r.embeddingModel = this.embeddingModel;
            r.baseUrl = this.baseUrl;
            r.temperature = this.temperature;
            r.maxTokens = this.maxTokens;
            return r;
        }
    }
}