package com.yourteam.ojaitester.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class EvaluationRow {
    private Long testCaseId;
    private String testCaseName;
    private String category;
    private String acResult;
    private String wrongCodeResults;
    private String comment;
    private final Map<String, String> submissionStatuses = new LinkedHashMap<>();

    public Long getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(Long testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAcResult() {
        return acResult;
    }

    public void setAcResult(String acResult) {
        this.acResult = acResult;
    }

    public String getWrongCodeResults() {
        return wrongCodeResults;
    }

    public void setWrongCodeResults(String wrongCodeResults) {
        this.wrongCodeResults = wrongCodeResults;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Map<String, String> getSubmissionStatuses() {
        return submissionStatuses;
    }
}
