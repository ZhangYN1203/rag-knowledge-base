package com.example.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(length = 100)
    private String extension;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String category = "default";

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "chunk_count")
    private Integer chunkCount = 0;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Boolean processed = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Document() {}

    public Document(Long id, String title, String filename, String extension, String content, String category, Long fileSize, Integer chunkCount, Long userId, Boolean processed, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.filename = filename;
        this.extension = extension;
        this.content = content;
        this.category = category;
        this.fileSize = fileSize;
        this.chunkCount = chunkCount;
        this.userId = userId;
        this.processed = processed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DocumentBuilder builder() {
        return new DocumentBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getChunkCount() { return chunkCount; }
    public void setChunkCount(Integer chunkCount) { this.chunkCount = chunkCount; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Boolean getProcessed() { return processed; }
    public void setProcessed(Boolean processed) { this.processed = processed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class DocumentBuilder {
        private Long id;
        private String title;
        private String filename;
        private String extension;
        private String content;
        private String category = "default";
        private Long fileSize;
        private Integer chunkCount = 0;
        private Long userId;
        private Boolean processed = false;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public DocumentBuilder id(Long id) { this.id = id; return this; }
        public DocumentBuilder title(String title) { this.title = title; return this; }
        public DocumentBuilder filename(String filename) { this.filename = filename; return this; }
        public DocumentBuilder extension(String extension) { this.extension = extension; return this; }
        public DocumentBuilder content(String content) { this.content = content; return this; }
        public DocumentBuilder category(String category) { this.category = category; return this; }
        public DocumentBuilder fileSize(Long fileSize) { this.fileSize = fileSize; return this; }
        public DocumentBuilder chunkCount(Integer chunkCount) { this.chunkCount = chunkCount; return this; }
        public DocumentBuilder userId(Long userId) { this.userId = userId; return this; }
        public DocumentBuilder processed(Boolean processed) { this.processed = processed; return this; }
        public DocumentBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public DocumentBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Document build() {
            return new Document(id, title, filename, extension, content, category, fileSize, chunkCount, userId, processed, createdAt, updatedAt);
        }
    }
}
