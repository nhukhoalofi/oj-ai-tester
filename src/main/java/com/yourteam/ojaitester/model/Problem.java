package com.yourteam.ojaitester.model;

import java.time.LocalDateTime;

public class Problem {
    private Long id;
    private String title;
    private String rawText;
    private String sourcePath;
    private String sourceType;
    private String status;
    private LocalDateTime createdAt;

    public Problem() {
    }

    public Problem(Long id, String title, String rawText, String sourcePath, String sourceType, String status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.rawText = rawText;
        this.sourcePath = sourcePath;
        this.sourceType = sourceType;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return title != null && !title.isBlank() ? title : "Problem #" + (id != null ? id : "?");
    }
}