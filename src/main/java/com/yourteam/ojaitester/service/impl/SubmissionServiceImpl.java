package com.yourteam.ojaitester.service.impl;

import com.yourteam.ojaitester.model.ExecutionResult;
import com.yourteam.ojaitester.model.Submission;
import com.yourteam.ojaitester.model.SubmissionRunReport;
import com.yourteam.ojaitester.model.TestCase;
import com.yourteam.ojaitester.repository.ExecutionResultRepository;
import com.yourteam.ojaitester.repository.SubmissionRepository;
import com.yourteam.ojaitester.repository.TestCaseRepository;
import com.yourteam.ojaitester.repository.impl.ExecutionResultRepositoryImpl;
import com.yourteam.ojaitester.repository.impl.SubmissionRepositoryImpl;
import com.yourteam.ojaitester.repository.impl.TestCaseRepositoryImpl;
import com.yourteam.ojaitester.service.SubmissionService;

import java.util.List;
import java.util.Optional;

public class SubmissionServiceImpl implements SubmissionService {

    private static final List<String> VALID_TYPES = List.of("AC", "WA", "TLE");

    private final SubmissionRepository submissionRepository = new SubmissionRepositoryImpl();
    private final TestCaseRepository testCaseRepository = new TestCaseRepositoryImpl();
    private final ExecutionResultRepository executionResultRepository = new ExecutionResultRepositoryImpl();
    private final CppJudgeService judgeService = new CppJudgeService();

    @Override
    public void validateSubmissionInput(Submission submission) {
        if (submission == null) {
            throw new IllegalArgumentException("Submission must not be null.");
        }
        if (submission.getProblemId() == null) {
            throw new IllegalArgumentException("Please select a problem.");
        }
        if (submission.getName() == null || submission.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Submission name must not be empty.");
        }
        if (submission.getSubmissionType() == null || !VALID_TYPES.contains(submission.getSubmissionType().trim().toUpperCase())) {
            throw new IllegalArgumentException("Submission type must be AC, WA, or TLE.");
        }
        if (submission.getSourceCode() == null || submission.getSourceCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Source code must not be empty.");
        }
    }

    @Override
    public Submission saveSubmission(Submission submission) {
        validateSubmissionInput(submission);
        submission.setSubmissionType(submission.getSubmissionType().trim().toUpperCase());
        if (submission.getLanguage() == null || submission.getLanguage().isBlank()) {
            submission.setLanguage("C++");
        }
        submission.setName(submission.getName().trim());
        submission.setSourceCode(submission.getSourceCode());
        return submissionRepository.save(submission);
    }

    @Override
    public List<Submission> getSubmissionsByProblemId(Long problemId) {
        if (problemId == null) {
            throw new IllegalArgumentException("Problem id must not be null.");
        }
        return submissionRepository.findByProblemId(problemId);
    }

    @Override
    public Optional<Submission> getSubmissionById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return submissionRepository.findById(id);
    }

    @Override
    public SubmissionRunReport runSubmissionOnTestCases(Submission submission) {
        Submission savedSubmission = saveSubmission(submission);
        List<TestCase> testCases = testCaseRepository.findByProblemId(savedSubmission.getProblemId());
        if (testCases.isEmpty()) {
            SubmissionRunReport emptyReport = new SubmissionRunReport();
            emptyReport.setSubmissionId(savedSubmission.getId());
            emptyReport.setSubmissionName(savedSubmission.getName());
            emptyReport.setTotalTestcases(0);
            emptyReport.setOverallStatus("NO_TESTCASE");
            emptyReport.setSummaryMessage("No test cases found for this problem.");
            return emptyReport;
        }

        executionResultRepository.deleteBySubmissionId(savedSubmission.getId());
        SubmissionRunReport report = judgeService.run(savedSubmission, testCases);
        for (ExecutionResult result : report.getResults()) {
            executionResultRepository.saveExecutionResult(result);
        }
        return report;
    }
}

