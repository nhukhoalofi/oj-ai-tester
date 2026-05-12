package com.yourteam.ojaitester.repository.impl;

import com.yourteam.ojaitester.config.DatabaseConfig;
import com.yourteam.ojaitester.model.TestCase;
import com.yourteam.ojaitester.repository.TestCaseRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TestCaseRepositoryImpl implements TestCaseRepository {

    @Override
    public List<TestCase> findByProblemId(Long problemId) {
        String sql = "SELECT * FROM test_cases WHERE problem_id = ? ORDER BY testcase_id ASC";
        List<TestCase> list = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, problemId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TestCase testCase = new TestCase();
                    testCase.setId(rs.getLong("testcase_id"));
                    testCase.setProblemId(rs.getLong("problem_id"));
                    testCase.setCategory(rs.getString("category"));
                    testCase.setInputData(rs.getString("input_data"));
                    testCase.setExpectedOutput(rs.getString("expected_output"));
                    testCase.setPurpose(rs.getString("purpose"));
                    Object strengthScore = rs.getObject("strength_score");
                    if (strengthScore != null) {
                        testCase.setStrengthScore(rs.getInt("strength_score"));
                    }
                    testCase.setGeneratedBy(rs.getString("generated_by"));
                    list.add(testCase);
                }
            }

            return list;
        } catch (Exception e) {
            throw new RuntimeException("Error loading test cases by problem id", e);
        }
    }
}

