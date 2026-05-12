package com.yourteam.ojaitester.service.impl;

import com.yourteam.ojaitester.model.EvaluationRow;
import com.yourteam.ojaitester.model.EvaluationSummary;
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
import com.yourteam.ojaitester.service.EvaluationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EvaluationServiceImpl implements EvaluationService {

    private final TestCaseRepository testCaseRepository = new TestCaseRepositoryImpl();
    private final SubmissionRepository submissionRepository = new SubmissionRepositoryImpl();
    private final ExecutionResultRepository executionResultRepository = new ExecutionResultRepositoryImpl();
    private final CppJudgeService judgeService = new CppJudgeService();

    @Override
    public EvaluationSummary evaluateProblem(Long problemId) {
        return evaluateProblem(problemId, null);
    }

    @Override
    public EvaluationSummary evaluateProblem(Long problemId, Consumer<String> progressCallback) {
        if (problemId == null) {
            throw new IllegalArgumentException("Please select a problem first.");
        }

        List<TestCase> testCases = testCaseRepository.findByProblemId(problemId);
        if (testCases.isEmpty()) {
            throw new IllegalArgumentException("No testcases found for this problem.");
        }

        List<Submission> submissions = submissionRepository.findByProblemId(problemId);
        List<Submission> acSubmissions = filterByType(submissions, "AC");
        List<Submission> wrongSubmissions = submissions.stream()
                .filter(submission -> isType(submission, "WA") || isType(submission, "TLE"))
                .toList();

        if (acSubmissions.isEmpty()) {
            throw new IllegalArgumentException("At least one AC solution is required for evaluation.");
        }
        if (wrongSubmissions.isEmpty()) {
            throw new IllegalArgumentException("At least one WA or TLE solution is required to evaluate testcase strength.");
        }

        List<Submission> orderedSubmissions = new ArrayList<>();
        orderedSubmissions.addAll(acSubmissions);
        orderedSubmissions.addAll(wrongSubmissions);

        Map<Long, SubmissionRunReport> reportBySubmissionId = new LinkedHashMap<>();
        for (int i = 0; i < orderedSubmissions.size(); i++) {
            Submission submission = orderedSubmissions.get(i);
            notifyProgress(progressCallback, "Running submission " + (i + 1) + "/" + orderedSubmissions.size()
                    + ": " + safeName(submission));
            executionResultRepository.deleteBySubmissionId(submission.getId());
            SubmissionRunReport report = judgeService.run(submission, testCases, 2000,
                    message -> notifyProgress(progressCallback, safeName(submission) + " - " + message));
            executionResultRepository.saveAll(report.getResults());
            reportBySubmissionId.put(submission.getId(), report);
        }

        return buildSummary(problemId, testCases, acSubmissions, wrongSubmissions, reportBySubmissionId);
    }

    private EvaluationSummary buildSummary(Long problemId,
                                           List<TestCase> testCases,
                                           List<Submission> acSubmissions,
                                           List<Submission> wrongSubmissions,
                                           Map<Long, SubmissionRunReport> reportBySubmissionId) {
        EvaluationSummary summary = new EvaluationSummary();
        summary.setProblemId(problemId);
        summary.setTotalTestcases(testCases.size());
        summary.setTotalAcSubmissions(acSubmissions.size());
        summary.setTotalWrongSubmissions(wrongSubmissions.size());

        Map<Long, Map<Long, ExecutionResult>> resultBySubmissionAndTestcase = indexResults(reportBySubmissionId);
        int acPassedTestcases = 0;

        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            EvaluationRow row = new EvaluationRow();
            row.setTestCaseId(testCase.getId());
            row.setTestCaseName("TC" + String.format("%02d", i + 1));
            row.setCategory(testCase.getCategory());

            boolean allAcSolutionsPass = true;
            List<String> acStatuses = new ArrayList<>();
            for (Submission acSubmission : acSubmissions) {
                String status = statusOf(resultBySubmissionAndTestcase, acSubmission.getId(), testCase.getId());
                row.getSubmissionStatuses().put(labelOf(acSubmission), status);
                acStatuses.add(labelOf(acSubmission) + ": " + status);
                if (!"AC".equals(status)) {
                    allAcSolutionsPass = false;
                }
            }
            row.setAcResult(String.join("; ", acStatuses));
            if (allAcSolutionsPass) {
                acPassedTestcases++;
            }

            int caughtWrongOnThisTestcase = 0;
            boolean caughtTle = false;
            List<String> wrongStatuses = new ArrayList<>();
            for (Submission wrongSubmission : wrongSubmissions) {
                String status = statusOf(resultBySubmissionAndTestcase, wrongSubmission.getId(), testCase.getId());
                row.getSubmissionStatuses().put(labelOf(wrongSubmission), status);
                wrongStatuses.add(labelOf(wrongSubmission) + ": " + status);
                if (isDetectedStatus(status)) {
                    caughtWrongOnThisTestcase++;
                }
                if ("TLE".equals(status)) {
                    caughtTle = true;
                }
            }
            row.setWrongCodeResults(String.join("; ", wrongStatuses));
            row.setComment(buildComment(allAcSolutionsPass, caughtWrongOnThisTestcase, caughtTle, wrongSubmissions.size()));
            summary.getRows().add(row);
        }

        int detectedWrongSubmissions = 0;
        int detectedWaSubmissions = 0;
        int detectedTleSubmissions = 0;
        for (Submission wrongSubmission : wrongSubmissions) {
            SubmissionRunReport report = reportBySubmissionId.get(wrongSubmission.getId());
            boolean detected = report != null && report.getResults().stream()
                    .map(ExecutionResult::getStatus)
                    .anyMatch(this::isDetectedStatus);
            if (detected) {
                detectedWrongSubmissions++;
                if (isType(wrongSubmission, "WA")) {
                    detectedWaSubmissions++;
                } else if (isType(wrongSubmission, "TLE")) {
                    detectedTleSubmissions++;
                }
            }
        }

        summary.setAcPassedTestcases(acPassedTestcases);
        summary.setDetectedWrongSubmissions(detectedWrongSubmissions);
        summary.setDetectedWaSubmissions(detectedWaSubmissions);
        summary.setDetectedTleSubmissions(detectedTleSubmissions);
        summary.setStrengthScore(wrongSubmissions.isEmpty()
                ? 0.0
                : detectedWrongSubmissions * 100.0 / wrongSubmissions.size());
        return summary;
    }

    private Map<Long, Map<Long, ExecutionResult>> indexResults(Map<Long, SubmissionRunReport> reportBySubmissionId) {
        Map<Long, Map<Long, ExecutionResult>> indexed = new HashMap<>();
        for (Map.Entry<Long, SubmissionRunReport> entry : reportBySubmissionId.entrySet()) {
            indexed.put(entry.getKey(), entry.getValue().getResults().stream()
                    .collect(Collectors.toMap(ExecutionResult::getTestcaseId, result -> result, (left, right) -> left)));
        }
        return indexed;
    }

    private List<Submission> filterByType(List<Submission> submissions, String type) {
        return submissions.stream().filter(submission -> isType(submission, type)).toList();
    }

    private boolean isType(Submission submission, String type) {
        return submission.getSubmissionType() != null
                && submission.getSubmissionType().trim().equalsIgnoreCase(type);
    }

    private boolean isDetectedStatus(String status) {
        return "WA".equals(status) || "TLE".equals(status) || "RE".equals(status);
    }

    private String statusOf(Map<Long, Map<Long, ExecutionResult>> indexed, Long submissionId, Long testcaseId) {
        ExecutionResult result = indexed.getOrDefault(submissionId, Map.of()).get(testcaseId);
        return result != null && result.getStatus() != null ? result.getStatus() : "-";
    }

    private String buildComment(boolean acPassed, int caughtWrongCount, boolean caughtTle, int totalWrongSubmissions) {
        if (!acPassed) {
            return "Testcase có thể sai expected output hoặc code AC không đúng";
        }
        if (caughtWrongCount >= Math.max(2, totalWrongSubmissions)) {
            return "Testcase mạnh";
        }
        if (caughtTle) {
            return "Bắt được lời giải chậm";
        }
        if (caughtWrongCount > 0) {
            return "Bắt được lời giải sai";
        }
        return "Testcase chưa đủ mạnh";
    }

    private String labelOf(Submission submission) {
        return safeName(submission) + " (" + submission.getSubmissionType() + ")";
    }

    private String safeName(Submission submission) {
        if (submission.getName() != null && !submission.getName().isBlank()) {
            return submission.getName();
        }
        return "Submission #" + submission.getId();
    }

    private void notifyProgress(Consumer<String> progressCallback, String message) {
        if (progressCallback != null) {
            progressCallback.accept(message);
        }
    }
}
