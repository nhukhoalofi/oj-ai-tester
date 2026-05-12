package com.yourteam.ojaitester.service;

import com.yourteam.ojaitester.model.EvaluationSummary;

import java.util.function.Consumer;

public interface EvaluationService {
    EvaluationSummary evaluateProblem(Long problemId);

    EvaluationSummary evaluateProblem(Long problemId, Consumer<String> progressCallback);

    default EvaluationSummary evaluateProblem(int problemId) {
        return evaluateProblem((long) problemId);
    }
}
