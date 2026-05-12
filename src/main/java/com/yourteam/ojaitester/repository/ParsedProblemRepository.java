package com.yourteam.ojaitester.repository;

import com.yourteam.ojaitester.model.ParsedProblem;

import java.util.Optional;

public interface ParsedProblemRepository {
    ParsedProblem saveOrUpdate(ParsedProblem parsedProblem);
    Optional<ParsedProblem> findByProblemId(Long problemId);
}
