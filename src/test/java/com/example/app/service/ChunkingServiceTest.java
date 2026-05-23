package com.example.app.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChunkingServiceTest {

    private final ChunkingService chunkingService = new ChunkingService();

    @Test
    void chunkByParagraph_shouldReturnEmpty_whenTextIsNull() {
        assertThat(chunkingService.chunkByParagraph(null, 100)).isEmpty();
    }

    @Test
    void chunkByParagraph_shouldReturnEmpty_whenTextIsEmpty() {
        assertThat(chunkingService.chunkByParagraph("", 100)).isEmpty();
    }

    @Test
    void chunkByParagraph_shouldReturnSingleChunk_whenTextIsShort() {
        List<String> chunks = chunkingService.chunkByParagraph("Hello world", 1000);
        assertThat(chunks).containsExactly("Hello world");
    }

    @Test
    void chunkByParagraph_shouldSplitByParagraphs() {
        String text = "First paragraph here.\n\nSecond paragraph here.\n\nThird paragraph here.";
        List<String> chunks = chunkingService.chunkByParagraph(text, 50);
        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0)).isEqualTo("First paragraph here.");
        assertThat(chunks.get(1)).isEqualTo("Second paragraph here.");
        assertThat(chunks.get(2)).isEqualTo("Third paragraph here.");
    }

    @Test
    void chunkByParagraph_shouldMergeSmallParagraphs() {
        String text = "Small A.\n\nSmall B.\n\nSmall C.";
        List<String> chunks = chunkingService.chunkByParagraph(text, 100);
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).contains("Small A.", "Small B.", "Small C.");
    }

    @Test
    void chunkByParagraph_shouldSplitOversizedParagraph() {
        String text = "这是第一句。这是第二句。这是第三句。这是第四句。这是第五句。";
        List<String> chunks = chunkingService.chunkByParagraph(text, 10);
        assertThat(chunks).isNotEmpty();
    }

    @Test
    void chunkByFixedSize_shouldReturnEmpty_whenTextIsNull() {
        assertThat(chunkingService.chunkByFixedSize(null, 100, 0)).isEmpty();
    }

    @Test
    void chunkByFixedSize_shouldReturnSingleChunk() {
        List<String> chunks = chunkingService.chunkByFixedSize("Short text", 100, 0);
        assertThat(chunks).containsExactly("Short text");
    }

    @Test
    void chunkByFixedSize_shouldSplitByFixedSize() {
        String text = "This is a longer text that should be split into multiple chunks by the fixed size algorithm.";
        List<String> chunks = chunkingService.chunkByFixedSize(text, 20, 0);
        assertThat(chunks).hasSizeGreaterThan(1);
        chunks.forEach(chunk -> assertThat(chunk.length()).isLessThanOrEqualTo(20));
    }

    @Test
    void chunkByFixedSize_shouldHandleOverlap() {
        String text = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        List<String> chunks = chunkingService.chunkByFixedSize(text, 10, 3);
        assertThat(chunks).hasSizeGreaterThan(1);
    }

    @Test
    void chunkByFixedSize_shouldHandleInvalidOverlap() {
        String text = "Some text here for testing purposes";
        // overlap >= chunkSize should be corrected internally
        List<String> chunks = chunkingService.chunkByFixedSize(text, 50, 100);
        assertThat(chunks).isNotEmpty();
    }

    @Test
    void chunkByParagraph_shouldNormalizeLineEndings() {
        String text = "Line1\r\n\r\nLine2\r\n\r\nLine3";
        List<String> chunks = chunkingService.chunkByParagraph(text, 10);
        assertThat(chunks).hasSize(3);
    }
}