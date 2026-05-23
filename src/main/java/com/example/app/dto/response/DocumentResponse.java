package com.example.app.dto.response;

import java.time.LocalDateTime;

public class DocumentResponse {

    private Long id;
    private String title;
    private String filename;
    private String extension;
    private String category;
    private Long fileSize;
    private Integer chunkCount;
    private Boolean processed;
    private LocalDateTime createdAt;

    public DocumentResponse() {}

    public DocumentResponse(Long id, String title, String filename, String extension, String category, Long fileSize, Integer chunkCount, Boolean processed, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.filename = filename;
        this.extension = extension;
        this.category = category;
        this.fileSize = fileSize;
        this.chunkCount = chunkCount;
        this.processed = processed;
        this.createdAt = createdAt;
    }

    public static DocumentResponseBuilder builder() {
        return new DocumentResponseBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getChunkCount() { return chunkCount; }
    public void setChunkCount(Integer chunkCount) { this.chunkCount = chunkCount; }

    public Boolean getProcessed() { return processed; }
    public void setProcessed(Boolean processed) { this.processed = processed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class DocumentResponseBuilder {
        private Long id;
        private String title;
        private String filename;
        private String extension;
        private String category;
        private Long fileSize;
        private Integer chunkCount;
        private Boolean processed;
        private LocalDateTime createdAt;

        public DocumentResponseBuilder id(Long id) { this.id = id; return this; }
        public DocumentResponseBuilder title(String title) { this.title = title; return this; }
        public DocumentResponseBuilder filename(String filename) { this.filename = filename; return this; }
        public DocumentResponseBuilder extension(String extension) { this.extension = extension; return this; }
        public DocumentResponseBuilder category(String category) { this.category = category; return this; }
        public DocumentResponseBuilder fileSize(Long fileSize) { this.fileSize = fileSize; return this; }
        public DocumentResponseBuilder chunkCount(Integer chunkCount) { this.chunkCount = chunkCount; return this; }
        public DocumentResponseBuilder processed(Boolean processed) { this.processed = processed; return this; }
        public DocumentResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public DocumentResponse build() {
            return new DocumentResponse(id, title, filename, extension, category, fileSize, chunkCount, processed, createdAt);
        }
    }
}
