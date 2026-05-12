package com.yourteam.ojaitester.repository.impl;

import com.yourteam.ojaitester.config.DatabaseConfig;
import com.yourteam.ojaitester.model.ExecutionResult;
import com.yourteam.ojaitester.repository.ExecutionResultRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ExecutionResultRepositoryImpl implements ExecutionResultRepository {

    @Override
    public ExecutionResult save(ExecutionResult result) {
        String sql = """
                INSERT INTO execution_results
                    (submission_id, problem_id, testcase_id, status, execution_time_ms,
                     memory_kb, actual_output, expected_output, error_message)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bind(ps, result);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    result.setId(rs.getLong(1));
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Cannot save execution results to database.", e);
        }
    }

    @Override
    public ExecutionResult saveExecutionResult(ExecutionResult result) {
        return save(result);
    }

    @Override
    public List<ExecutionResult> saveAll(List<ExecutionResult> results) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        String sql = """
                INSERT INTO execution_results
                    (submission_id, problem_id, testcase_id, status, execution_time_ms,
                     memory_kb, actual_output, expected_output, error_message)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (ExecutionResult result : results) {
                    bind(ps, result);
                    ps.addBatch();
                }
                ps.executeBatch();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    int index = 0;
                    while (keys.next() && index < results.size()) {
                        results.get(index).setId(keys.getLong(1));
                        index++;
                    }
                }
                conn.commit();
                return results;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot save execution results to database.", e);
        }
    }

    @Override
    public List<ExecutionResult> findByProblemId(Long problemId) {
        String sql = """
                SELECT er.result_id, er.submission_id, er.problem_id, er.testcase_id,
                       tc.category, tc.input_data, er.expected_output, er.status,
                       er.execution_time_ms, er.memory_kb, er.actual_output, er.error_message
                FROM execution_results er
                LEFT JOIN test_cases tc ON tc.testcase_id = er.testcase_id
                WHERE er.problem_id = ?
                ORDER BY er.result_id DESC
                """;
        return queryById(sql, problemId);
    }

    @Override
    public List<ExecutionResult> findBySubmissionId(Long submissionId) {
        String sql = """
                SELECT er.result_id, er.submission_id, er.problem_id, er.testcase_id,
                       tc.category, tc.input_data, er.expected_output, er.status,
                       er.execution_time_ms, er.memory_kb, er.actual_output, er.error_message
                FROM execution_results er
                LEFT JOIN test_cases tc ON tc.testcase_id = er.testcase_id
                WHERE er.submission_id = ?
                ORDER BY er.result_id ASC
                """;
        return queryById(sql, submissionId);
    }

    @Override
    public void deleteByProblemId(Long problemId) {
        String sql = "DELETE FROM execution_results WHERE problem_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, problemId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting execution results by problem id", e);
        }
    }

    @Override
    public void deleteBySubmissionId(Long submissionId) {
        String sql = "DELETE FROM execution_results WHERE submission_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, submissionId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting execution results by submission id", e);
        }
    }

    private void bind(PreparedStatement ps, ExecutionResult result) throws Exception {
        if (result.getSubmissionId() == null) {
            ps.setNull(1, Types.BIGINT);
        } else {
            ps.setLong(1, result.getSubmissionId());
        }
        ps.setLong(2, result.getProblemId());
        ps.setLong(3, result.getTestcaseId());
        ps.setString(4, result.getStatus());
        if (result.getExecutionTimeMs() == null) {
            ps.setNull(5, Types.INTEGER);
        } else {
            ps.setInt(5, result.getExecutionTimeMs());
        }
        if (result.getMemoryKb() == null) {
            ps.setNull(6, Types.INTEGER);
        } else {
            ps.setInt(6, result.getMemoryKb());
        }
        ps.setString(7, result.getActualOutput());
        ps.setString(8, result.getExpectedOutput());
        ps.setString(9, result.getErrorMessage());
    }

    private List<ExecutionResult> queryById(String sql, Long id) {
        List<ExecutionResult> results = new ArrayList<>();
        if (id == null) {
            return results;
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(map(rs));
                }
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Error loading execution results", e);
        }
    }

    private ExecutionResult map(ResultSet rs) throws Exception {
        ExecutionResult result = new ExecutionResult();
        result.setId(rs.getLong("result_id"));
        long submissionId = rs.getLong("submission_id");
        if (!rs.wasNull()) {
            result.setSubmissionId(submissionId);
        }
        result.setProblemId(rs.getLong("problem_id"));
        result.setTestcaseId(rs.getLong("testcase_id"));
        result.setCategory(rs.getString("category"));
        result.setInputData(rs.getString("input_data"));
        result.setExpectedOutput(rs.getString("expected_output"));
        result.setStatus(rs.getString("status"));

        int executionTimeMs = rs.getInt("execution_time_ms");
        if (!rs.wasNull()) {
            result.setExecutionTimeMs(executionTimeMs);
        }
        int memoryKb = rs.getInt("memory_kb");
        if (!rs.wasNull()) {
            result.setMemoryKb(memoryKb);
        }
        result.setActualOutput(rs.getString("actual_output"));
        result.setErrorMessage(rs.getString("error_message"));
        return result;
    }
}
