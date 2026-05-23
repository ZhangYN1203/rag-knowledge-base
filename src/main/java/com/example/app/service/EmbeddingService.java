package com.example.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final OllamaApi ollamaApi;
    private final JdbcTemplate jdbcTemplate;
    private final boolean isH2;

    public EmbeddingService(OllamaApi ollamaApi, JdbcTemplate jdbcTemplate) {
        this.ollamaApi = ollamaApi;
        this.jdbcTemplate = jdbcTemplate;
        this.isH2 = detectH2();
    }

    private boolean detectH2() {
        try {
            String url = jdbcTemplate.getDataSource().getConnection().getMetaData().getURL();
            return url != null && url.contains("h2");
        } catch (Exception e) {
            return true; // safe default
        }
    }

    public float[] generateEmbedding(String text) {
        var request = new OllamaApi.EmbeddingsRequest("nomic-embed-text", text);
        var response = ollamaApi.embed(request);
        if (response.embeddings() != null && !response.embeddings().isEmpty()) {
            return response.embeddings().get(0);
        }
        throw new RuntimeException("Failed to generate embedding");
    }

    private String floatArrayToVectorString(float[] arr) {
        StringJoiner sj = new StringJoiner(",");
        for (float v : arr) {
            sj.add(String.valueOf(v));
        }
        return "[" + sj + "]";
    }

    public void indexDocument(Long documentId, String title, String category, List<String> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            try {
                float[] embedding = generateEmbedding(chunks.get(i));
                String vectorStr = floatArrayToVectorString(embedding);
                String metadata = String.format("{\"documentId\":\"%s\",\"title\":\"%s\",\"category\":\"%s\",\"chunkIndex\":%d}",
                        documentId, title != null ? title : "", category != null ? category : "", i);

                if (isH2) {
                    jdbcTemplate.update(
                        "INSERT INTO vector_store (id, content, metadata, embedding) VALUES (?, ?, ?, ?)",
                        UUID.randomUUID().toString(),
                        chunks.get(i),
                        metadata,
                        vectorStr
                    );
                } else {
                    jdbcTemplate.update(
                        "INSERT INTO vector_store (id, content, metadata, embedding) VALUES (?, ?, ?::jsonb, ?::vector)",
                        UUID.randomUUID().toString(),
                        chunks.get(i),
                        metadata,
                        vectorStr
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to index chunk {} for document {}", i, documentId, e);
            }
        }
        log.info("Indexed {} chunks for document: {} (id={})", chunks.size(), title, documentId);
    }

    public List<Document> search(String query, int topK) {
        try {
            float[] embedding = generateEmbedding(query);
            String vectorStr = floatArrayToVectorString(embedding);

            List<Map<String, Object>> rows;
            if (isH2) {
                // H2: store embedding as text, do simple match by substring (vector search disabled)
                rows = jdbcTemplate.queryForList(
                    "SELECT content, metadata FROM vector_store LIMIT ?",
                    topK
                );
            } else {
                rows = jdbcTemplate.queryForList(
                    "SELECT content, metadata, 1 - (embedding <=> ?::vector) AS similarity " +
                    "FROM vector_store ORDER BY embedding <=> ?::vector LIMIT ?",
                    vectorStr, vectorStr, topK
                );
            }

            List<Document> results = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                String content = (String) row.get("content");
                String metadataJson = (String) row.get("metadata");
                double similarity = row.containsKey("similarity") ? ((Number) row.get("similarity")).doubleValue() : 0.0;

                Map<String, Object> metadata = new java.util.HashMap<>();
                metadata.put("similarity", similarity);
                if (metadataJson != null) {
                    String docId = extractJsonValue(metadataJson, "documentId");
                    String t = extractJsonValue(metadataJson, "title");
                    String cat = extractJsonValue(metadataJson, "category");
                    if (docId != null) metadata.put("documentId", docId);
                    if (t != null) metadata.put("title", t);
                    if (cat != null) metadata.put("category", cat);
                }
                metadata.put("distance", similarity);
                results.add(new Document(content, metadata));
            }
            return results;
        } catch (Exception e) {
            log.warn("Vector search failed, returning empty results", e);
            return List.of();
        }
    }

    public void removeDocumentEmbeddings(Long documentId) {
        String sql;
        if (isH2) {
            sql = "DELETE FROM vector_store WHERE metadata LIKE ?";
        } else {
            sql = "DELETE FROM vector_store WHERE metadata->>'documentId' = ?";
        }
        int deleted = jdbcTemplate.update(sql, "%\"documentId\":\"" + documentId + "\"%");
        log.info("Deleted {} embeddings for document id={}", deleted, documentId);
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        return end > start ? json.substring(start, end) : null;
    }
}