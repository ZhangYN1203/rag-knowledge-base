package com.example.app.service;

import com.example.app.entity.Document;
import com.example.app.exception.BusinessException;
import com.example.app.repository.DocumentRepository;
import com.example.app.dto.response.DocumentResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".docx", ".txt", ".md");

    private final DocumentRepository documentRepository;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;

    public DocumentService(DocumentRepository documentRepository,
                           ChunkingService chunkingService,
                           EmbeddingService embeddingService) {
        this.documentRepository = documentRepository;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
    }

    private final String uploadDir = "uploads/documents";

    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, String category, Long userId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
                : "";

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的文件类型: " + extension + "。支持的类型: " + ALLOWED_EXTENSIONS);
        }

        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(uploadDir, filename);

        Files.createDirectories(filePath.getParent());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String content = extractText(file, extension);

        List<String> chunks = chunkingService.chunkByParagraph(content, 1000);

        Document document = Document.builder()
                .title(originalFilename != null ? originalFilename : "untitled")
                .filename(filename)
                .extension(extension)
                .content(content)
                .category(category)
                .fileSize(file.getSize())
                .chunkCount(chunks.size())
                .userId(userId)
                .processed(true)
                .build();

        documentRepository.save(document);

        // Index embeddings in vector store
        try {
            embeddingService.indexDocument(document.getId(), document.getTitle(), category, chunks);
            log.info("Successfully indexed {} chunks for document {}", chunks.size(), document.getId());
        } catch (Exception e) {
            log.error("Failed to index embeddings for document {}", document.getId(), e);
            document.setProcessed(false);
            documentRepository.save(document);
        }

        return toResponse(document);
    }

    private String extractText(MultipartFile file, String extension) throws IOException {
        if (extension.equals(".pdf")) {
            try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } else if (extension.equals(".docx")) {
            try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
                StringBuilder content = new StringBuilder();
                for (XWPFParagraph paragraph : document.getParagraphs()) {
                    content.append(paragraph.getText()).append("\n");
                }
                return content.toString();
            }
        } else {
            return new String(file.getBytes());
        }
    }

    public List<DocumentResponse> getUserDocuments(Long userId) {
        List<Document> userDocs = documentRepository.findByUserId(userId);
        List<Document> sharedDocs = documentRepository.findByUserId(0L);
        // Merge shared docs with user docs (user's own docs first)
        List<Document> allDocs = new java.util.ArrayList<>(userDocs);
        for (Document shared : sharedDocs) {
            if (allDocs.stream().noneMatch(d -> d.getId().equals(shared.getId()))) {
                allDocs.add(shared);
            }
        }
        return allDocs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocument(Long id, Long userId) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        if (document.getUserId().equals(0L)) {
            throw new RuntimeException("系统文档不可删除");
        }

        if (!document.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该文档");
        }

        // Remove embeddings
        try {
            embeddingService.removeDocumentEmbeddings(id);
        } catch (Exception e) {
            log.warn("Failed to remove embeddings for document {}", id, e);
        }

        documentRepository.delete(document);
    }

    private DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .filename(document.getFilename())
                .extension(document.getExtension())
                .category(document.getCategory())
                .fileSize(document.getFileSize())
                .chunkCount(document.getChunkCount())
                .processed(document.getProcessed())
                .createdAt(document.getCreatedAt())
                .build();
    }
}