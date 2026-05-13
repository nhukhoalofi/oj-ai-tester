package com.yourteam.ojaitester.model;

import java.time.LocalDateTime;

public class Submission {
    private Long id;
    private Long problemId;
    private String name;
    private String submissionType;
    private String language;
    private String sourceCode;
    private String note;
    private LocalDateTime createdAt;

    public Submission() {
    }

    public Submission(Long id, Long problemId, String name, String submissionType, String language, String sourceCode, LocalDateTime createdAt) {
        this.id = id;
        this.problemId = problemId;
        this.name = name;
        this.submissionType = submissionType;
        this.language = language;
        this.sourceCode = sourceCode;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubmissionType() {
        return submissionType;
    }

    public void setSubmissionType(String submissionType) {
        this.submissionType = submissionType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name != null && !name.isBlank() ? name : "Submission #" + (id != null ? id : "?");
    }
}

