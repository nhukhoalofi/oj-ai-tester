package com.yourteam.ojaitester.repository.impl;

import com.yourteam.ojaitester.config.DatabaseConfig;
import com.yourteam.ojaitester.model.GeneratedTestCase;
import com.yourteam.ojaitester.model.TestCase;
import com.yourteam.ojaitester.repository.TestCaseRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class TestCaseRepositoryImpl implements TestCaseRepository {

    @Override
    public GeneratedTestCase save(GeneratedTestCase testCase) {
        if (testCase == null || testCase.getProblemId() == null) {
            throw new IllegalArgumentException("Testcase and problem id must not be null.");
        }

        String sql = """
                INSERT INTO test_cases
                    (problem_id, category, input_data, expected_output, purpose, strength_score, generated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindGeneratedTestCase(ps, testCase);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    testCase.setId(rs.getLong(1));
                }
            }
            return testCase;
        } catch (Exception e) {
            throw new RuntimeException("Cannot save testcases to database: " + rootMessage(e), e);
        }
    }

    @Override
    public List<GeneratedTestCase> saveAll(Long problemId, List<GeneratedTestCase> testCases) {
        if (problemId == null) {
            throw new IllegalArgumentException("Problem id must not be null.");
        }
        if (testCases == null || testCases.isEmpty()) {
            return List.of();
        }

        String sql = """
                INSERT INTO test_cases
                    (problem_id, category, input_data, expected_output, purpose, strength_score, generated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (GeneratedTestCase testCase : testCases) {
                    testCase.setProblemId(problemId);
                    bindGeneratedTestCase(ps, testCase);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            testCase.setId(keys.getLong(1));
                        }
                    }
                }
                conn.commit();
                return testCases;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot save testcases to database: " + rootMessage(e), e);
        }
    }

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

    @Override
    public void deleteByProblemId(Long problemId) {
        String deleteExecutionResults = """
                DELETE FROM execution_results
                WHERE testcase_id IN (SELECT testcase_id FROM test_cases WHERE problem_id = ?)
                """;
        String deleteTestCases = "DELETE FROM test_cases WHERE problem_id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(deleteExecutionResults);
                 PreparedStatement ps2 = conn.prepareStatement(deleteTestCases)) {
                ps1.setLong(1, problemId);
                ps1.executeUpdate();

                ps2.setLong(1, problemId);
                ps2.executeUpdate();

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot delete testcases from database.", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteExecutionResults = "DELETE FROM execution_results WHERE testcase_id = ?";
        String deleteTestCase = "DELETE FROM test_cases WHERE testcase_id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(deleteExecutionResults);
                 PreparedStatement ps2 = conn.prepareStatement(deleteTestCase)) {
                ps1.setLong(1, id);
                ps1.executeUpdate();

                ps2.setLong(1, id);
                ps2.executeUpdate();

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot delete testcase from database.", e);
        }
    }

    private void bindGeneratedTestCase(PreparedStatement ps, GeneratedTestCase testCase) throws Exception {
        ps.setLong(1, testCase.getProblemId());
        ps.setString(2, normalizeCategory(testCase.getCategory()));
        ps.setString(3, testCase.getInputData());
        ps.setString(4, testCase.getExpectedOutput());
        ps.setString(5, testCase.getPurpose());
        if (testCase.getStrengthScore() == null) {
            ps.setNull(6, Types.INTEGER);
        } else {
            ps.setInt(6, testCase.getStrengthScore());
        }
        ps.setString(7, testCase.getGeneratedBy() == null ? "GEMINI" : testCase.getGeneratedBy());
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "basic";
        }
        String normalized = category.trim().toLowerCase();
        if (normalized.contains("sample")) return "sample";
        if (normalized.contains("edge")) return "edge";
        if (normalized.contains("random")) return "random";
        if (normalized.contains("max")) return "max";
        if (normalized.contains("tricky")) return "tricky";
        if (normalized.contains("basic")) return "basic";
        return normalized.length() <= 100 ? normalized : normalized.substring(0, 100);
    }

    private String rootMessage(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor.getCause() != null) {
            cursor = cursor.getCause();
        }
        return cursor.getMessage() != null ? cursor.getMessage() : cursor.toString();
    }
}

