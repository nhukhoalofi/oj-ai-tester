package com.yourteam.ojaitester.model;

public class EvaluationResult {
    private Long submissionId;
    private String submissionName;
    private String submissionType;
    private SubmissionRunReport runReport;

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public String getSubmissionName() {
        return submissionName;
    }

    public void setSubmissionName(String submissionName) {
        this.submissionName = submissionName;
    }

    public String getSubmissionType() {
        return submissionType;
    }

    public void setSubmissionType(String submissionType) {
        this.submissionType = submissionType;
    }

    public SubmissionRunReport getRunReport() {
        return runReport;
    }

    public void setRunReport(SubmissionRunReport runReport) {
        this.runReport = runReport;
    }
}
