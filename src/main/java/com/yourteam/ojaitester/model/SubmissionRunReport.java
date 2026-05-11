package com.yourteam.ojaitester.model;

import java.util.ArrayList;
import java.util.List;

public class SubmissionRunReport {
    private Long submissionId;
    private String submissionName;
    private String overallStatus;
    private int totalTestcases;
    private int passedCount;
    private int wrongAnswerCount;
    private int tleCount;
    private int runtimeErrorCount;
    private int compileErrorCount;
    private int totalRuntimeMs;
    private String summaryMessage;
    private final List<ExecutionResult> results = new ArrayList<>();

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

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public int getTotalTestcases() {
        return totalTestcases;
    }

    public void setTotalTestcases(int totalTestcases) {
        this.totalTestcases = totalTestcases;
    }

    public int getPassedCount() {
        return passedCount;
    }

    public void setPassedCount(int passedCount) {
        this.passedCount = passedCount;
    }

    public int getWrongAnswerCount() {
        return wrongAnswerCount;
    }

    public void setWrongAnswerCount(int wrongAnswerCount) {
        this.wrongAnswerCount = wrongAnswerCount;
    }

    public int getTleCount() {
        return tleCount;
    }

    public void setTleCount(int tleCount) {
        this.tleCount = tleCount;
    }

    public int getRuntimeErrorCount() {
        return runtimeErrorCount;
    }

    public void setRuntimeErrorCount(int runtimeErrorCount) {
        this.runtimeErrorCount = runtimeErrorCount;
    }

    public int getCompileErrorCount() {
        return compileErrorCount;
    }

    public void setCompileErrorCount(int compileErrorCount) {
        this.compileErrorCount = compileErrorCount;
    }

    public int getTotalRuntimeMs() {
        return totalRuntimeMs;
    }

    public void setTotalRuntimeMs(int totalRuntimeMs) {
        this.totalRuntimeMs = totalRuntimeMs;
    }

    public int getFailedCount() {
        return wrongAnswerCount + tleCount + runtimeErrorCount + compileErrorCount;
    }

    public String getSummaryMessage() {
        return summaryMessage;
    }

    public void setSummaryMessage(String summaryMessage) {
        this.summaryMessage = summaryMessage;
    }

    public List<ExecutionResult> getResults() {
        return results;
    }
}

