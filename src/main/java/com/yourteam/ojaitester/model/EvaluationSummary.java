package com.yourteam.ojaitester.model;

import java.util.ArrayList;
import java.util.List;

public class EvaluationSummary {
    private Long problemId;
    private int totalTestcases;
    private int acPassedTestcases;
    private int totalAcSubmissions;
    private int totalWrongSubmissions;
    private int detectedWrongSubmissions;
    private int detectedWaSubmissions;
    private int detectedTleSubmissions;
    private double strengthScore;
    private final List<EvaluationRow> rows = new ArrayList<>();

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public int getTotalTestcases() {
        return totalTestcases;
    }

    public void setTotalTestcases(int totalTestcases) {
        this.totalTestcases = totalTestcases;
    }

    public int getAcPassedTestcases() {
        return acPassedTestcases;
    }

    public void setAcPassedTestcases(int acPassedTestcases) {
        this.acPassedTestcases = acPassedTestcases;
    }

    public int getTotalAcSubmissions() {
        return totalAcSubmissions;
    }

    public void setTotalAcSubmissions(int totalAcSubmissions) {
        this.totalAcSubmissions = totalAcSubmissions;
    }

    public int getTotalWrongSubmissions() {
        return totalWrongSubmissions;
    }

    public void setTotalWrongSubmissions(int totalWrongSubmissions) {
        this.totalWrongSubmissions = totalWrongSubmissions;
    }

    public int getDetectedWrongSubmissions() {
        return detectedWrongSubmissions;
    }

    public void setDetectedWrongSubmissions(int detectedWrongSubmissions) {
        this.detectedWrongSubmissions = detectedWrongSubmissions;
    }

    public int getDetectedWaSubmissions() {
        return detectedWaSubmissions;
    }

    public void setDetectedWaSubmissions(int detectedWaSubmissions) {
        this.detectedWaSubmissions = detectedWaSubmissions;
    }

    public int getDetectedTleSubmissions() {
        return detectedTleSubmissions;
    }

    public void setDetectedTleSubmissions(int detectedTleSubmissions) {
        this.detectedTleSubmissions = detectedTleSubmissions;
    }

    public double getStrengthScore() {
        return strengthScore;
    }

    public void setStrengthScore(double strengthScore) {
        this.strengthScore = strengthScore;
    }

    public List<EvaluationRow> getRows() {
        return rows;
    }
}
