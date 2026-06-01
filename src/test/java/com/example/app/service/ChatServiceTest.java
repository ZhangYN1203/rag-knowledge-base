package com.example.app.service;

import com.example.app.config.ModelProvider;
import com.example.app.dto.request.ChatRequest;
import com.example.app.dto.response.ChatResponse;
import com.example.app.dto.response.ConversationSummary;
import com.example.app.entity.ChatMessage;
import com.example.app.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatServiceTest {

    @Mock
    private ChatModel chatModel;
    @Mock
    private EmbeddingService embeddingService;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private TemplateService templateService;
    @Mock
    private ConfigService configService;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        ModelProvider modelProvider = new ModelProvider(chatModel, null);
        chatService = new ChatService(modelProvider, embeddingService, chatMessageRepository, templateService);
    }

    @Test
    void chat_shouldReturnResponse() {
        ChatRequest request = new ChatRequest("Hello", null);
        when(embeddingService.search("Hello", 5)).thenReturn(List.of());
        when(templateService.renderTemplateByName(eq("rag-default"), any())).thenThrow(new RuntimeException("not found"));

        Generation generation = new Generation(new AssistantMessage("Hello! How can I help you?"));
        org.springframework.ai.chat.model.ChatResponse aiResponse =
                new org.springframework.ai.chat.model.ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(aiResponse);

        ChatResponse response = chatService.chat(request, 1L);

        assertThat(response.getAnswer()).isEqualTo("Hello! How can I help you?");
        assertThat(response.getConversationId()).isNotNull();
        verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
    }

    @Test
    void chat_shouldUseExistingConversationId() {
        ChatRequest request = new ChatRequest("Hi", "existing-conv-id");
        when(embeddingService.search("Hi", 5)).thenReturn(List.of());
        when(templateService.renderTemplateByName(eq("rag-default"), any())).thenThrow(new RuntimeException("not found"));

        Generation generation = new Generation(new AssistantMessage("Hi there!"));
        org.springframework.ai.chat.model.ChatResponse aiResponse =
                new org.springframework.ai.chat.model.ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(aiResponse);

        ChatResponse response = chatService.chat(request, 1L);

        assertThat(response.getConversationId()).isEqualTo("existing-conv-id");
    }

    @Test
    void chat_shouldIncludeSources_whenRelevantDocsExist() {
        ChatRequest request = new ChatRequest("test query", null);
        Document doc = new Document("Relevant content here for testing purpose.",
                Map.of("title", "doc1", "distance", 0.95));
        when(embeddingService.search("test query", 5)).thenReturn(List.of(doc));
        when(templateService.renderTemplateByName(eq("rag-default"), any())).thenThrow(new RuntimeException("not found"));

        Generation generation = new Generation(new AssistantMessage("Answer based on docs."));
        org.springframework.ai.chat.model.ChatResponse aiResponse =
                new org.springframework.ai.chat.model.ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(aiResponse);

        ChatResponse response = chatService.chat(request, 1L);

        assertThat(response.getSources()).isNotEmpty();
        assertThat(response.getSources().get(0).getTitle()).isEqualTo("doc1");
    }

    @Test
    void getConversationHistory_shouldReturnMessages() {
        List<ChatMessage> messages = List.of(
                ChatMessage.builder().conversationId("conv1").userId(1L).role("user").content("Hi").build(),
                ChatMessage.builder().conversationId("conv1").userId(1L).role("assistant").content("Hello").build()
        );
        when(chatMessageRepository.findByConversationId("conv1")).thenReturn(messages);

        List<ChatMessage> result = chatService.getConversationHistory("conv1");

        assertThat(result).hasSize(2);
    }

    @Test
    void getUserConversations_shouldReturnSummaries() {
        when(chatMessageRepository.findDistinctConversationIdsByUserId(1L)).thenReturn(List.of("conv1", "conv2"));

        ChatMessage userMsg1 = ChatMessage.builder()
                .conversationId("conv1").userId(1L).role("user")
                .content("What is RAG?").createdAt(LocalDateTime.now().minusMinutes(10)).build();
        ChatMessage assistantMsg1 = ChatMessage.builder()
                .conversationId("conv1").userId(1L).role("assistant")
                .content("RAG stands for...").createdAt(LocalDateTime.now().minusMinutes(9)).build();
        when(chatMessageRepository.findByConversationId("conv1")).thenReturn(List.of(userMsg1, assistantMsg1));

        ChatMessage userMsg2 = ChatMessage.builder()
                .conversationId("conv2").userId(1L).role("user")
                .content("Hi").createdAt(LocalDateTime.now().minusMinutes(5)).build();
        ChatMessage assistantMsg2 = ChatMessage.builder()
                .conversationId("conv2").userId(1L).role("assistant")
                .content("Hello!").createdAt(LocalDateTime.now().minusMinutes(4)).build();
        when(chatMessageRepository.findByConversationId("conv2")).thenReturn(List.of(userMsg2, assistantMsg2));

        List<ConversationSummary> summaries = chatService.getUserConversations(1L);

        assertThat(summaries).hasSize(2);
        assertThat(summaries.get(0).getConversationId()).isEqualTo("conv2");
    }

    @Test
    void getUserConversations_shouldTruncateLongTitles() {
        String longTitle = "A".repeat(100);
        when(chatMessageRepository.findDistinctConversationIdsByUserId(1L)).thenReturn(List.of("conv1"));

        ChatMessage msg = ChatMessage.builder()
                .conversationId("conv1").userId(1L).role("user")
                .content(longTitle).createdAt(LocalDateTime.now()).build();
        when(chatMessageRepository.findByConversationId("conv1")).thenReturn(List.of(msg));

        List<ConversationSummary> summaries = chatService.getUserConversations(1L);

        assertThat(summaries.get(0).getTitle()).hasSize(53);
    }

    @Test
    void deleteConversation_shouldDeleteMessages() {
        ChatMessage msg = ChatMessage.builder()
                .conversationId("conv1").userId(1L).role("user").content("Hi").build();
        when(chatMessageRepository.findByConversationId("conv1")).thenReturn(List.of(msg));

        chatService.deleteConversation("conv1", 1L);
        verify(chatMessageRepository).deleteByConversationId("conv1");
    }

    @Test
    void chatStreamSse_shouldReturnEmitter() {
        ChatRequest request = new ChatRequest("Hello", null);
        lenient().when(embeddingService.search("Hello", 5)).thenReturn(List.of());
        lenient().when(templateService.renderTemplateByName(eq("rag-default"), any())).thenThrow(new RuntimeException("not found"));

        Generation gen = new Generation(new AssistantMessage("Hello!"));
        var aiResponse = new org.springframework.ai.chat.model.ChatResponse(List.of(gen));
        Flux<org.springframework.ai.chat.model.ChatResponse> flux = Flux.just(aiResponse);
        lenient().when(chatModel.stream(any(Prompt.class))).thenReturn(flux);

        var emitter = chatService.chatStreamSse(request, 1L);

        assertThat(emitter).isNotNull();
    }
}