package com.yourteam.ojaitester.repository;

import com.yourteam.ojaitester.model.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository {
    Submission save(Submission submission);
    List<Submission> findByProblemId(Long problemId);
    List<Submission> findByProblemIdAndType(Long problemId, String submissionType);
    default List<Submission> findByProblemIdAndType(int problemId, String submissionType) {
        return findByProblemIdAndType((long) problemId, submissionType);
    }
    void deleteByProblemIdAndType(Long problemId, String submissionType);
    default void deleteByProblemIdAndType(int problemId, String submissionType) {
        deleteByProblemIdAndType((long) problemId, submissionType);
    }
    Optional<Submission> findById(Long id);
}

