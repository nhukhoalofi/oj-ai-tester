package com.yourteam.ojaitester.repository;

import com.yourteam.ojaitester.model.TestCase;

import java.util.List;

public interface TestCaseRepository {
    List<TestCase> findByProblemId(Long problemId);
}

