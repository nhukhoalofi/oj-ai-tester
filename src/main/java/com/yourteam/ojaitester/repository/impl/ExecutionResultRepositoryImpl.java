package com.yourteam.ojaitester.repository.impl;

import com.yourteam.ojaitester.config.DatabaseConfig;
import com.yourteam.ojaitester.model.ExecutionResult;
import com.yourteam.ojaitester.repository.ExecutionResultRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class ExecutionResultRepositoryImpl implements ExecutionResultRepository {

    @Override
    public ExecutionResult saveExecutionResult(ExecutionResult result) {
        String sql = "INSERT INTO execution_results (submission_id, testcase_id, status, execution_time_ms, memory_kb, actual_output, error_message) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, result.getSubmissionId());
            ps.setLong(2, result.getTestcaseId());
            ps.setString(3, result.getStatus());
            if (result.getExecutionTimeMs() != null) {
                ps.setInt(4, result.getExecutionTimeMs());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            if (result.getMemoryKb() != null) {
                ps.setInt(5, result.getMemoryKb());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.setString(6, result.getActualOutput());
            ps.setString(7, result.getErrorMessage());
            ps.executeUpdate();

            try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    result.setId(rs.getLong(1));
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error saving execution result", e);
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
}

