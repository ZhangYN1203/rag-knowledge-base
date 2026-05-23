package com.example.app.service;

import com.example.app.dto.request.PromptTemplateRequest;
import com.example.app.dto.response.PromptTemplateResponse;
import com.example.app.entity.PromptTemplate;
import com.example.app.repository.PromptTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private PromptTemplateRepository repository;

    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new TemplateService(repository);
    }

    @Test
    void list_shouldReturnAllTemplates() {
        when(repository.findAll()).thenReturn(List.of(
                PromptTemplate.builder().id(1L).name("t1").content("content1").build(),
                PromptTemplate.builder().id(2L).name("t2").content("content2").build()
        ));

        List<PromptTemplateResponse> result = templateService.list(null);

        assertThat(result).hasSize(2);
    }

    @Test
    void list_shouldFilterByCategory() {
        when(repository.findByCategory("rag")).thenReturn(List.of(
                PromptTemplate.builder().id(1L).name("t1").category("rag").content("c1").build()
        ));

        List<PromptTemplateResponse> result = templateService.list("rag");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("t1");
    }

    @Test
    void getById_shouldSucceed() {
        PromptTemplate template = PromptTemplate.builder()
                .id(1L).name("test").content("Hello {{name}}").build();
        when(repository.findById(1L)).thenReturn(Optional.of(template));

        PromptTemplateResponse result = templateService.getById(1L);

        assertThat(result.getName()).isEqualTo("test");
    }

    @Test
    void getById_shouldFail_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.getById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("模板不存在");
    }

    @Test
    void getByName_shouldSucceed() {
        PromptTemplate template = PromptTemplate.builder()
                .id(1L).name("rag-default").content("context").build();
        when(repository.findByName("rag-default")).thenReturn(Optional.of(template));

        PromptTemplateResponse result = templateService.getByName("rag-default");

        assertThat(result.getName()).isEqualTo("rag-default");
    }

    @Test
    void create_shouldSucceed() {
        PromptTemplateRequest request = new PromptTemplateRequest();
        request.setName("new-template");
        request.setContent("Answer: {{query}}");
        request.setCategory("qa");

        when(repository.existsByName("new-template")).thenReturn(false);
        when(repository.save(any(PromptTemplate.class))).thenAnswer(invocation -> {
            PromptTemplate t = invocation.getArgument(0);
            return PromptTemplate.builder()
                    .id(1L)
                    .name(t.getName())
                    .category(t.getCategory())
                    .content(t.getContent())
                    .variables(t.getVariables())
                    .usageCount(0)
                    .createdAt(LocalDateTime.now())
                    .build();
        });

        PromptTemplateResponse result = templateService.create(request);

        assertThat(result.getName()).isEqualTo("new-template");
        assertThat(result.getCategory()).isEqualTo("qa");
        assertThat(result.getVariables()).contains("query");
        verify(repository).save(any(PromptTemplate.class));
    }

    @Test
    void create_shouldFail_whenNameExists() {
        PromptTemplateRequest request = new PromptTemplateRequest();
        request.setName("dup");
        request.setContent("content");

        when(repository.existsByName("dup")).thenReturn(true);

        assertThatThrownBy(() -> templateService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("模板名称已存在");
        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldSucceed() {
        PromptTemplate existing = PromptTemplate.builder()
                .id(1L).name("old").content("old content").category("default").usageCount(0)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(PromptTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PromptTemplateRequest request = new PromptTemplateRequest();
        request.setName("updated");
        request.setContent("new content {{var}}");
        request.setCategory("new-cat");

        PromptTemplateResponse result = templateService.update(1L, request);

        assertThat(result.getName()).isEqualTo("updated");
        assertThat(result.getContent()).isEqualTo("new content {{var}}");
    }

    @Test
    void delete_shouldSucceed() {
        PromptTemplate template = PromptTemplate.builder().id(1L).name("t").content("c").build();
        when(repository.findById(1L)).thenReturn(Optional.of(template));

        templateService.delete(1L);

        verify(repository).delete(template);
    }

    @Test
    void delete_shouldFail_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.delete(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("模板不存在");
    }

    @Test
    void renderTemplateByName_shouldReplaceVariables() {
        PromptTemplate template = PromptTemplate.builder()
                .id(1L).name("greet").content("Hello {{name}}, {{greeting}}!")
                .usageCount(0).build();
        when(repository.findByName("greet")).thenReturn(Optional.of(template));
        when(repository.save(any(PromptTemplate.class))).thenReturn(template);

        String result = templateService.renderTemplateByName("greet", Map.of("name", "World", "greeting", "hi"));

        assertThat(result).isEqualTo("Hello World, hi!");
    }

    @Test
    void renderTemplate_shouldIncrementUsageCount() {
        PromptTemplate template = PromptTemplate.builder()
                .id(1L).name("t").content("Hello {{name}}!").usageCount(5).build();
        when(repository.findById(1L)).thenReturn(Optional.of(template));
        when(repository.save(any(PromptTemplate.class))).thenReturn(template);

        templateService.renderTemplate(1L, Map.of("name", "Test"));

        verify(repository).save(argThat(t -> t.getUsageCount() == 6));
    }

    @Test
    void renderContent_shouldKeepUnknownVariables() {
        String result = templateService.renderContent("Hi {{unknown}}!", Map.of());

        assertThat(result).isEqualTo("Hi {{unknown}}!");
    }

    @Test
    void extractVariables_shouldFindPlaceholders() {
        List<String> vars = templateService.extractVariables("Hello {{name}}, you are {{age}} years old");

        assertThat(vars).containsExactlyInAnyOrder("name", "age");
    }

    @Test
    void extractVariables_shouldReturnEmpty_whenNoPlaceholders() {
        List<String> vars = templateService.extractVariables("Plain text without variables");

        assertThat(vars).isEmpty();
    }

    @Test
    void extractVariablesAsJson_shouldProduceJson() {
        String json = templateService.extractVariablesAsJson("Hello {{name}} and {{place}}");

        assertThat(json).isEqualTo("[\"name\", \"place\"]");
    }
}