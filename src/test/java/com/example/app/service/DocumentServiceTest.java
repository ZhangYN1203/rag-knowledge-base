package com.example.app.service;

import com.example.app.dto.response.DocumentResponse;
import com.example.app.entity.Document;
import com.example.app.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private ChunkingService chunkingService;
    @Mock
    private EmbeddingService embeddingService;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(documentRepository, chunkingService, embeddingService);
    }

    @Test
    void uploadDocument_shouldSucceed() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getSize()).thenReturn(100L);
        when(file.getBytes()).thenReturn("Hello World".getBytes());
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("Hello World".getBytes()));
        when(chunkingService.chunkByParagraph("Hello World", 1000)).thenReturn(List.of("Hello World"));

        Document savedDoc = Document.builder()
                .id(1L)
                .title("test.txt")
                .filename("uuid.txt")
                .extension(".txt")
                .content("Hello World")
                .category("general")
                .fileSize(100L)
                .chunkCount(1)
                .userId(1L)
                .processed(true)
                .build();
        when(documentRepository.save(any(Document.class))).thenReturn(savedDoc);

        DocumentResponse response = documentService.uploadDocument(file, "general", 1L);

        assertThat(response.getTitle()).isEqualTo("test.txt");
        assertThat(response.getProcessed()).isTrue();
        verify(embeddingService).indexDocument(any(), any(), any(), anyList());
    }

    @Test
    void uploadDocument_shouldSetProcessedFalse_whenEmbeddingFails() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getSize()).thenReturn(5L);
        when(file.getBytes()).thenReturn("Hello".getBytes());
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("Hello".getBytes()));
        when(chunkingService.chunkByParagraph("Hello", 1000)).thenReturn(List.of("Hello"));
        doThrow(new RuntimeException("Ollama not available"))
                .when(embeddingService).indexDocument(any(), any(), any(), anyList());

        Document savedDoc = Document.builder()
                .id(1L).title("test.txt").filename("uuid.txt").processed(true).build();
        Document failedDoc = Document.builder()
                .id(1L).title("test.txt").filename("uuid.txt").processed(false).build();

        when(documentRepository.save(any(Document.class)))
                .thenReturn(savedDoc)
                .thenReturn(failedDoc);

        DocumentResponse response = documentService.uploadDocument(file, "general", 1L);

        assertThat(response.getProcessed()).isFalse();
    }

    @Test
    void getUserDocuments_shouldReturnList() {
        when(documentRepository.findByUserId(1L)).thenReturn(List.of(
                Document.builder().id(1L).title("doc1").filename("f1").content("c1").userId(1L).processed(true).build(),
                Document.builder().id(2L).title("doc2").filename("f2").content("c2").userId(1L).processed(true).build()
        ));

        List<DocumentResponse> docs = documentService.getUserDocuments(1L);

        assertThat(docs).hasSize(2);
        assertThat(docs.get(0).getTitle()).isEqualTo("doc1");
    }

    @Test
    void deleteDocument_shouldSucceed() {
        Document doc = Document.builder().id(1L).title("test").filename("f").userId(1L).build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        documentService.deleteDocument(1L, 1L);

        verify(embeddingService).removeDocumentEmbeddings(1L);
        verify(documentRepository).delete(doc);
    }

    @Test
    void deleteDocument_shouldFail_whenNotOwner() {
        Document doc = Document.builder().id(1L).title("test").filename("f").userId(2L).build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> documentService.deleteDocument(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("无权删除该文档");
        verify(documentRepository, never()).delete(any());
    }

    @Test
    void deleteDocument_shouldFail_whenNotFound() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.deleteDocument(99L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("文档不存在");
    }

    @Test
    void deleteDocument_shouldContinueEvenIfEmbeddingRemovalFails() {
        Document doc = Document.builder().id(1L).title("test").filename("f").userId(1L).build();
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));
        doThrow(new RuntimeException("DB error")).when(embeddingService).removeDocumentEmbeddings(1L);

        documentService.deleteDocument(1L, 1L);

        verify(documentRepository).delete(doc);
    }
}