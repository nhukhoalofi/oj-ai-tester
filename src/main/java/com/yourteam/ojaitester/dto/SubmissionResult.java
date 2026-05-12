package com.yourteam.ojaitester.dto;

public class SubmissionResult {
    public enum Verdict {
        AC("Accepted"),
        WA("Wrong Answer"),
        TLE("Time Limit Exceeded"),
        RE("Runtime Error"),
        CE("Compilation Error");

        private final String label;

        Verdict(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private Verdict verdict;
    private String expectedOutput;
    private String actualOutput;
    private long executionTime;
    private String errorMessage;

    public SubmissionResult() {
    }

    public SubmissionResult(Verdict verdict, String expectedOutput, String actualOutput, long executionTime) {
        this.verdict = verdict;
        this.expectedOutput = expectedOutput;
        this.actualOutput = actualOutput;
        this.executionTime = executionTime;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public void setActualOutput(String actualOutput) {
        this.actualOutput = actualOutput;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "SubmissionResult{" +
                "verdict=" + verdict +
                ", executionTime=" + executionTime + "ms" +
                '}';
    }
}

