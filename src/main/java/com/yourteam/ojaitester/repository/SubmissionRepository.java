package com.yourteam.ojaitester.repository;

import com.yourteam.ojaitester.model.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository {
    Submission save(Submission submission);
    List<Submission> findByProblemId(Long problemId);
    Optional<Submission> findById(Long id);
}

