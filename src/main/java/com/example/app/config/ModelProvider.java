package com.example.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class ModelProvider {

    private static final Logger log = LoggerFactory.getLogger(ModelProvider.class);

    private final ChatModel chatModel;

    public ModelProvider(ChatModel chatModel) {
        this.chatModel = chatModel;
        log.info("ModelProvider initialized with: {}", chatModel.getClass().getSimpleName());
    }

    public ChatModel getChatModel() {
        return chatModel;
    }

    public void refreshModel() {
        log.info("Model refresh requested (requires restart to change provider)");
    }
}