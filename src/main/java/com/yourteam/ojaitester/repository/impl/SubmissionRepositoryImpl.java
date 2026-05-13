package com.yourteam.ojaitester.repository.impl;

import com.yourteam.ojaitester.config.DatabaseConfig;
import com.yourteam.ojaitester.model.Submission;
import com.yourteam.ojaitester.repository.SubmissionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubmissionRepositoryImpl implements SubmissionRepository {

    private Submission mapSubmission(ResultSet rs) throws SQLException {
        Submission submission = new Submission();
        submission.setId(rs.getLong("submission_id"));
        submission.setProblemId(rs.getLong("problem_id"));
        submission.setName(rs.getString("name"));
        submission.setSubmissionType(rs.getString("submission_type"));
        submission.setLanguage(rs.getString("language"));
        submission.setSourceCode(rs.getString("source_code"));
        submission.setNote(rs.getString("note"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            submission.setCreatedAt(createdAt.toLocalDateTime());
        }

        return submission;
    }

    @Override
    public Submission save(Submission submission) {
        boolean isInsert = submission.getId() == null;
        String sql = isInsert
                ? "INSERT INTO submissions (problem_id, name, submission_type, language, source_code, note) VALUES (?, ?, ?, ?, ?, ?)"
                : "UPDATE submissions SET problem_id = ?, name = ?, submission_type = ?, language = ?, source_code = ?, note = ?, updated_at = GETDATE() WHERE submission_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, submission.getProblemId());
            ps.setString(2, submission.getName());
            ps.setString(3, submission.getSubmissionType());
            ps.setString(4, submission.getLanguage());
            ps.setString(5, submission.getSourceCode());
            ps.setString(6, submission.getNote());
            if (!isInsert) {
                ps.setLong(7, submission.getId());
            }

            ps.executeUpdate();

            if (isInsert) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        submission.setId(keys.getLong(1));
                    }
                }
            }

            return submission;
        } catch (Exception e) {
            throw new RuntimeException("Error saving submission", e);
        }
    }

    @Override
    public List<Submission> findByProblemId(Long problemId) {
        String sql = "SELECT * FROM submissions WHERE problem_id = ? ORDER BY created_at DESC, submission_id DESC";
        List<Submission> list = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, problemId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSubmission(rs));
                }
            }

            return list;
        } catch (Exception e) {
            throw new RuntimeException("Error loading submissions by problem id", e);
        }
    }

    @Override
    public List<Submission> findByProblemIdAndType(Long problemId, String submissionType) {
        String sql = """
                SELECT * FROM submissions
                WHERE problem_id = ? AND UPPER(submission_type) = UPPER(?)
                ORDER BY created_at DESC, submission_id DESC
                """;
        List<Submission> list = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, problemId);
            ps.setString(2, submissionType);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSubmission(rs));
                }
            }

            return list;
        } catch (Exception e) {
            throw new RuntimeException("Error loading submissions by problem id and type", e);
        }
    }

    @Override
    public void deleteByProblemIdAndType(Long problemId, String submissionType) {
        String deleteExecutionResults = """
                DELETE FROM execution_results
                WHERE submission_id IN (
                    SELECT submission_id FROM submissions
                    WHERE problem_id = ? AND UPPER(submission_type) = UPPER(?)
                )
                """;
        String deleteSubmissions = "DELETE FROM submissions WHERE problem_id = ? AND UPPER(submission_type) = UPPER(?)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(deleteExecutionResults);
                 PreparedStatement ps2 = conn.prepareStatement(deleteSubmissions)) {
                ps1.setLong(1, problemId);
                ps1.setString(2, submissionType);
                ps1.executeUpdate();

                ps2.setLong(1, problemId);
                ps2.setString(2, submissionType);
                ps2.executeUpdate();

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error deleting submissions by problem id and type", e);
        }
    }

    @Override
    public Optional<Submission> findById(Long id) {
        String sql = "SELECT * FROM submissions WHERE submission_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSubmission(rs));
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Error loading submission by id", e);
        }
    }
}

