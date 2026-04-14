package com.yourteam.ojaitester.service;

import com.yourteam.ojaitester.model.Problem;

import java.util.List;
import java.util.Optional;

public interface ProblemService {
    Problem createProblem(Problem problem);
    List<Problem> getAllProblems();
    Optional<Problem> getProblemById(Long id);
    void deleteProblem(Long id);
}