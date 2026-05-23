package com.example.app.service;

import com.example.app.dto.response.ChatResponse;
import com.example.app.dto.response.ConversationSummary;
import com.example.app.entity.ChatMessage;
import com.example.app.repository.ChatMessageRepository;
import com.example.app.config.ModelProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final long SSE_TIMEOUT = 300_000L; // 5 minutes

    private final ModelProvider modelProvider;
    private final EmbeddingService embeddingService;
    private final ChatMessageRepository chatMessageRepository;
    private final TemplateService templateService;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(ModelProvider modelProvider,
                       EmbeddingService embeddingService,
                       ChatMessageRepository chatMessageRepository,
                       TemplateService templateService) {
        this.modelProvider = modelProvider;
        this.embeddingService = embeddingService;
        this.chatMessageRepository = chatMessageRepository;
        this.templateService = templateService;
    }

    @Transactional
    public ChatResponse chat(com.example.app.dto.request.ChatRequest request, Long userId) {
        String conversationId = resolveConversationId(request);
        ChatModel chatModel = modelProvider.getChatModel();

        // Vector search for context
        List<Document> relevantDocs = embeddingService.search(request.getMessage(), 5);
        List<ChatResponse.ReferenceSource> sources = toSources(relevantDocs);

        // Build prompt with context
        String systemPrompt = buildSystemPrompt(relevantDocs);
        Prompt prompt = new Prompt(
                new SystemMessage(systemPrompt),
                new UserMessage(request.getMessage())
        );

        org.springframework.ai.chat.model.ChatResponse aiResponse = chatModel.call(prompt);
        Generation result = aiResponse.getResult();
        String answer = result != null && result.getOutput() != null
                ? result.getOutput().getText()
                : "";

        saveMessage(conversationId, userId, "user", request.getMessage());
        saveMessage(conversationId, userId, "assistant", answer);

        return ChatResponse.builder()
                .conversationId(conversationId)
                .answer(answer)
                .sources(sources)
                .modelName(chatModel.getClass().getSimpleName())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public SseEmitter chatStreamSse(com.example.app.dto.request.ChatRequest request, Long userId) {
        String conversationId = resolveConversationId(request);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        executor.submit(() -> {
            try {
                ChatModel chatModel = modelProvider.getChatModel();

                // 1. Vector search
                List<Document> relevantDocs = embeddingService.search(request.getMessage(), 5);
                List<ChatResponse.ReferenceSource> sources = toSources(relevantDocs);

                // 2. Build prompt
                String systemPrompt = buildSystemPrompt(relevantDocs);
                Prompt prompt = new Prompt(
                        new SystemMessage(systemPrompt),
                        new UserMessage(request.getMessage())
                );

                // 3. Save user message
                saveMessage(conversationId, userId, "user", request.getMessage());

                // 4. Stream tokens
                StringBuilder fullAnswer = new StringBuilder();
                org.springframework.ai.chat.model.ChatResponse lastResponse = null;

                var flux = chatModel.stream(prompt);
                var iterator = flux.toStream().iterator();

                while (iterator.hasNext()) {
                    org.springframework.ai.chat.model.ChatResponse chunk = iterator.next();
                    lastResponse = chunk;
                    Generation gen = chunk.getResult();
                    if (gen != null && gen.getOutput() != null) {
                        String token = gen.getOutput().getText();
                        if (token != null && !token.isEmpty()) {
                            fullAnswer.append(token);
                            sendEvent(emitter, "token", token);
                        }
                    }
                }

                // 5. Send references
                sendEvent(emitter, "references", sources);

                // 6. Send metadata
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("modelName", chatModel.getClass().getSimpleName());
                sendEvent(emitter, "metadata", metadata);

                // 7. Save assistant message BEFORE sending done
                saveMessage(conversationId, userId, "assistant", fullAnswer.toString());

                // 8. Send done
                sendEvent(emitter, "done", conversationId);

                emitter.complete();
            } catch (Exception e) {
                log.error("SSE stream error", e);
                try {
                    sendEvent(emitter, "error", e.getMessage());
                } catch (IOException ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) throws IOException {
        String json;
        if (data instanceof String s) {
            json = objectMapper.writeValueAsString(s);
            // For plain text tokens, send raw string (strip quotes)
            if ("token".equals(eventName)) {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(s));
                return;
            }
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(json));
        } else {
            json = objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(json));
        }
    }

    private List<ChatResponse.ReferenceSource> toSources(List<Document> docs) {
        return docs.stream()
                .map(doc -> {
                    String content = doc.getText();
                    String excerpt = content.length() > 200
                            ? content.substring(0, 200) + "..."
                            : content;
                    return new ChatResponse.ReferenceSource(
                            (String) doc.getMetadata().getOrDefault("title", "Unknown"),
                            excerpt,
                            (Double) doc.getMetadata().getOrDefault("distance", 0.0)
                    );
                })
                .collect(Collectors.toList());
    }

    private String buildSystemPrompt(List<Document> relevantDocs) {
        String templateContent;
        try {
            templateContent = templateService.renderTemplateByName("rag-default",
                    Map.of("query", ""));
        } catch (Exception e) {
            log.warn("Failed to render prompt template, using fallback", e);
            StringBuilder sb = new StringBuilder();
            sb.append("你是一个智能问答助手。请根据以下参考资料回答问题。\n\n");

            if (relevantDocs.isEmpty()) {
                sb.append("没有提供参考资料，请根据你的知识回答。\n\n");
            } else {
                sb.append("参考资料：\n\n");
                for (int i = 0; i < relevantDocs.size(); i++) {
                    Document doc = relevantDocs.get(i);
                    sb.append("--- 参考资料 ").append(i + 1).append(" ---\n");
                    sb.append("来源：").append(doc.getMetadata().getOrDefault("title", "Unknown")).append("\n");
                    sb.append(doc.getText()).append("\n\n");
                }
            }

            sb.append("请基于以上资料，用中文简洁明了地回答问题。");
            sb.append("如果资料中没有相关信息，请如实说明。");
            sb.append("回答时标注引用来源编号，如 [1][2]。");

            return sb.toString();
        }

        // Fill template with context
        StringBuilder contextBuilder = new StringBuilder();
        if (!relevantDocs.isEmpty()) {
            for (int i = 0; i < relevantDocs.size(); i++) {
                Document doc = relevantDocs.get(i);
                contextBuilder.append("--- 参考资料 ").append(i + 1).append(" ---\n");
                contextBuilder.append("来源：").append(doc.getMetadata().getOrDefault("title", "Unknown")).append("\n");
                contextBuilder.append(doc.getText()).append("\n\n");
            }
        }

        return templateContent.replace("{{context}}", contextBuilder.toString())
                .replace("{{query}}", "");
    }

    private String resolveConversationId(com.example.app.dto.request.ChatRequest request) {
        String conversationId = request.getConversationId();
        return (conversationId == null || conversationId.isBlank())
                ? UUID.randomUUID().toString()
                : conversationId;
    }

    private void saveMessage(String conversationId, Long userId, String role, String content) {
        ChatMessage message = ChatMessage.builder()
                .conversationId(conversationId)
                .userId(userId)
                .role(role)
                .content(content)
                .build();
        chatMessageRepository.save(message);
    }

    public List<ChatMessage> getConversationHistory(String conversationId) {
        return chatMessageRepository.findByConversationId(conversationId);
    }

    public List<ConversationSummary> getUserConversations(Long userId) {
        List<String> conversationIds = chatMessageRepository.findDistinctConversationIdsByUserId(userId);
        List<ConversationSummary> summaries = new ArrayList<>();

        for (String convId : conversationIds) {
            List<ChatMessage> messages = chatMessageRepository.findByConversationId(convId);
            if (messages.isEmpty()) continue;

            ChatMessage first = messages.get(0);
            ChatMessage last = messages.get(messages.size() - 1);

            String title = messages.stream()
                    .filter(m -> "user".equals(m.getRole()))
                    .map(ChatMessage::getContent)
                    .findFirst()
                    .orElse("New conversation");
            if (title.length() > 50) title = title.substring(0, 50) + "...";

            summaries.add(ConversationSummary.builder()
                    .conversationId(convId)
                    .title(title)
                    .createdAt(first.getCreatedAt())
                    .lastMessageAt(last.getCreatedAt())
                    .messageCount(messages.size())
                    .build());
        }

        summaries.sort((a, b) -> b.getLastMessageAt().compareTo(a.getLastMessageAt()));
        return summaries;
    }

    public void deleteConversation(String conversationId, Long userId) {
        chatMessageRepository.deleteByConversationId(conversationId);
    }
}