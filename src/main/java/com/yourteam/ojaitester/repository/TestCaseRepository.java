package com.yourteam.ojaitester.repository;

import com.yourteam.ojaitester.model.TestCase;
import com.yourteam.ojaitester.model.GeneratedTestCase;

import java.util.List;

public interface TestCaseRepository {
    GeneratedTestCase save(GeneratedTestCase testCase);
    List<GeneratedTestCase> saveAll(Long problemId, List<GeneratedTestCase> testCases);
    default List<GeneratedTestCase> saveAll(int problemId, List<GeneratedTestCase> testCases) {
        return saveAll((long) problemId, testCases);
    }
    List<TestCase> findByProblemId(Long problemId);
    default List<TestCase> findByProblemId(int problemId) {
        return findByProblemId((long) problemId);
    }
    void deleteByProblemId(Long problemId);
    default void deleteByProblemId(int problemId) {
        deleteByProblemId((long) problemId);
    }
    void deleteById(Long id);
    default void deleteById(int id) {
        deleteById((long) id);
    }
}

