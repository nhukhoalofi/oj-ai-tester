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
                ? "INSERT INTO submissions (problem_id, name, submission_type, language, source_code) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE submissions SET problem_id = ?, name = ?, submission_type = ?, language = ?, source_code = ? WHERE submission_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, submission.getProblemId());
            ps.setString(2, submission.getName());
            ps.setString(3, submission.getSubmissionType());
            ps.setString(4, submission.getLanguage());
            ps.setString(5, submission.getSourceCode());
            if (!isInsert) {
                ps.setLong(6, submission.getId());
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

