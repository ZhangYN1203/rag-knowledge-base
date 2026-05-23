package com.example.app.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    /**
     * Split text into chunks by paragraphs, then merge small paragraphs
     * to target chunk size.
     */
    public List<String> chunkByParagraph(String text, int maxChunkSize) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        text = text.replaceAll("\\r\\n", "\n");
        String[] paragraphs = text.split("\n\\s*\n");
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;

            if (currentChunk.length() + trimmed.length() > maxChunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
            }

            if (trimmed.length() > maxChunkSize) {
                // Split oversized paragraph by sentences
                List<String> subChunks = splitBySentences(trimmed, maxChunkSize);
                for (String sub : subChunks) {
                    if (currentChunk.length() > 0) {
                        chunks.add(currentChunk.toString().trim());
                        currentChunk = new StringBuilder();
                    }
                    chunks.add(sub);
                }
            } else {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(trimmed);
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * Split text into fixed-size chunks with overlap.
     */
    public List<String> chunkByFixedSize(String text, int chunkSize, int overlap) {
        if (text == null || text.isEmpty()) return List.of();
        if (chunkSize <= 0) chunkSize = 500;
        if (overlap < 0) overlap = 0;
        if (overlap >= chunkSize) overlap = chunkSize / 4;

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            // Try to break at a sentence boundary
            if (end < text.length()) {
                int sentenceEnd = findSentenceBoundary(text, end);
                if (sentenceEnd > start) {
                    end = sentenceEnd;
                }
            }

            chunks.add(text.substring(start, end).trim());
            start = end - overlap;
            if (start >= text.length()) break;
            if (start < 0) start = 0;
        }

        return chunks;
    }

    private List<String> splitBySentences(String text, int maxSize) {
        List<String> sentences = new ArrayList<>();
        String[] parts = text.split("(?<=[。！？.!?])\\s*");
        StringBuilder current = new StringBuilder();

        for (String part : parts) {
            if (current.length() + part.length() > maxSize && current.length() > 0) {
                sentences.add(current.toString().trim());
                current = new StringBuilder();
            }
            current.append(part);
        }
        if (current.length() > 0) {
            sentences.add(current.toString().trim());
        }

        return sentences.isEmpty() ? List.of(text) : sentences;
    }

    private int findSentenceBoundary(String text, int fromPos) {
        String punctuations = "。！？.!?\n";
        for (int i = fromPos; i > Math.max(fromPos - 100, 0); i--) {
            if (punctuations.indexOf(text.charAt(i)) >= 0) {
                return i + 1;
            }
        }
        return fromPos;
    }
}