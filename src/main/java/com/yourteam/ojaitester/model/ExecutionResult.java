package com.yourteam.ojaitester.model;

public class ExecutionResult {
    private Long id;
    private Long submissionId;
    private Long testcaseId;
    private String inputData;
    private String expectedOutput;
    private String status;
    private Integer executionTimeMs;
    private Integer memoryKb;
    private String actualOutput;
    private String errorMessage;

    public ExecutionResult() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Long getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(Long testcaseId) {
        this.testcaseId = testcaseId;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Integer executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public Integer getMemoryKb() {
        return memoryKb;
    }

    public void setMemoryKb(Integer memoryKb) {
        this.memoryKb = memoryKb;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public void setActualOutput(String actualOutput) {
        this.actualOutput = actualOutput;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

