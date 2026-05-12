package com.yourteam.ojaitester.repository;

import com.yourteam.ojaitester.model.ExecutionResult;

public interface ExecutionResultRepository {
    ExecutionResult saveExecutionResult(ExecutionResult result);
    void deleteBySubmissionId(Long submissionId);
}

