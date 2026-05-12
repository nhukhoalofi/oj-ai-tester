package com.yourteam.ojaitester.service;

import com.yourteam.ojaitester.model.ParsedProblem;
import com.yourteam.ojaitester.model.Problem;

import java.util.Optional;

public interface ProblemAnalysisService {
    ParsedProblem analyzeAndSave(Problem problem);
    Optional<ParsedProblem> getParsedProblem(Long problemId);
}
