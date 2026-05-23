package com.example.app.config;

import com.example.app.entity.Document;
import com.example.app.repository.DocumentRepository;
import com.example.app.service.ChunkingService;
import com.example.app.service.EmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class KnowledgeBaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseInitializer.class);

    private final DocumentRepository documentRepository;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;

    public KnowledgeBaseInitializer(DocumentRepository documentRepository,
                                    ChunkingService chunkingService,
                                    EmbeddingService embeddingService) {
        this.documentRepository = documentRepository;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
    }

    @Override
    public void run(String... args) {
        // Only initialize if no documents exist yet
        if (documentRepository.count() > 0) {
            log.info("Documents already exist, skipping knowledge base initialization");
            return;
        }

        final Long SAMPLE_USER_ID = 0L; // shared/system documents visible to all

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:sample-knowledge/*.txt");

            if (resources.length == 0) {
                log.warn("No sample knowledge files found at classpath:sample-knowledge/");
                return;
            }

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null) continue;

                String rawName = filename.replace(".txt", "");
                // Map English filenames to Chinese display titles
                String title = switch (rawName) {
                    case "cloud-computing" -> "云计算简介";
                    case "java-basics" -> "Java编程基础";
                    case "rag-technology" -> "RAG技术原理";
                    case "spring-framework" -> "Spring框架概述";
                    default -> rawName;
                };
                String content;
                try (InputStream is = resource.getInputStream()) {
                    content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }

                // Chunk and index
                List<String> chunks = chunkingService.chunkByParagraph(content, 1000);

                Document document = Document.builder()
                        .title(title)
                        .filename(filename)
                        .extension(".txt")
                        .content(content)
                        .category("sample")
                        .fileSize((long) content.length())
                        .chunkCount(chunks.size())
                        .userId(SAMPLE_USER_ID)
                        .processed(true)
                        .build();

                document = documentRepository.saveAndFlush(document);

                // Index embeddings in vector store
                try {
                    embeddingService.indexDocument(document.getId(), document.getTitle(), "sample", chunks);
                    log.info("Indexed sample document: {} ({} chunks)", title, chunks.size());
                } catch (Exception e) {
                    log.error("Failed to index embeddings for sample document {}", title, e);
                    document.setProcessed(false);
                    documentRepository.saveAndFlush(document);
                }
            }

            log.info("Knowledge base initialized with {} sample documents", resources.length);
        } catch (Exception e) {
            log.error("Failed to initialize knowledge base", e);
        }
    }
}