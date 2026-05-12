package com.yourteam.ojaitester.repository;

import com.yourteam.ojaitester.model.ExecutionResult;

import java.util.List;

public interface ExecutionResultRepository {
    ExecutionResult save(ExecutionResult result);
    ExecutionResult saveExecutionResult(ExecutionResult result);
    List<ExecutionResult> saveAll(List<ExecutionResult> results);
    List<ExecutionResult> findByProblemId(Long problemId);
    default List<ExecutionResult> findByProblemId(int problemId) {
        return findByProblemId((long) problemId);
    }
    List<ExecutionResult> findBySubmissionId(Long submissionId);
    default List<ExecutionResult> findBySubmissionId(int submissionId) {
        return findBySubmissionId((long) submissionId);
    }
    void deleteByProblemId(Long problemId);
    default void deleteByProblemId(int problemId) {
        deleteByProblemId((long) problemId);
    }
    void deleteBySubmissionId(Long submissionId);
    default void deleteBySubmissionId(int submissionId) {
        deleteBySubmissionId((long) submissionId);
    }
}
