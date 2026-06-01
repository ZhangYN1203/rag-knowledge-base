package com.example.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class ModelProvider {

    private static final Logger log = LoggerFactory.getLogger(ModelProvider.class);

    private final ChatModel chatModel;
    private final EmbeddingModel embeddingModel;

    public ModelProvider(ChatModel chatModel, EmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        log.info("ModelProvider initialized with chat: {}, embedding: {}",
                chatModel.getClass().getSimpleName(),
                embeddingModel.getClass().getSimpleName());
    }

    public ChatModel getChatModel() {
        return chatModel;
    }

    public EmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }

    public void refreshModel() {
        log.info("Model refresh requested (requires restart to change provider)");
    }
}