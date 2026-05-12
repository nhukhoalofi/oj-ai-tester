package com.yourteam.ojaitester.service;

import com.yourteam.ojaitester.dto.SubmissionResult;
import com.yourteam.ojaitester.dto.TestCase;
import com.yourteam.ojaitester.model.Submission;
import com.yourteam.ojaitester.model.SubmissionRunReport;

import java.util.List;
import java.util.Optional;

public interface SubmissionService {
    void validateSubmissionInput(Submission submission);
    Submission saveSubmission(Submission submission);
    List<Submission> getSubmissionsByProblemId(Long problemId);
    Optional<Submission> getSubmissionById(Long id);
    SubmissionRunReport runSubmissionOnTestCases(Submission submission);
    SubmissionResult submitCode(String code, TestCase testCase, String problemName);
}

