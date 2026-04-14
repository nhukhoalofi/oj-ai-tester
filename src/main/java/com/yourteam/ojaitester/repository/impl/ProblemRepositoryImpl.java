package com.yourteam.ojaitester.repository.impl;

import com.yourteam.ojaitester.config.DatabaseConfig;
import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.repository.ProblemRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProblemRepositoryImpl implements ProblemRepository {

    @Override
    public Problem save(Problem problem) {
        String sql = """
                INSERT INTO problems (title, raw_text, source_path, source_type, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, problem.getTitle());
            ps.setString(2, problem.getRawText());
            ps.setString(3, problem.getSourcePath());
            ps.setString(4, problem.getSourceType());
            ps.setString(5, problem.getStatus());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    problem.setId(rs.getLong(1));
                }
            }

            return problem;

        } catch (Exception e) {
            throw new RuntimeException("Error saving problem", e);
        }
    }

    @Override
    public List<Problem> findAll() {
        String sql = "SELECT * FROM problems ORDER BY problem_id DESC";
        List<Problem> list = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Problem p = new Problem();
                p.setId(rs.getLong("problem_id"));
                p.setTitle(rs.getString("title"));
                p.setRawText(rs.getString("raw_text"));
                p.setSourcePath(rs.getString("source_path"));
                p.setSourceType(rs.getString("source_type"));
                p.setStatus(rs.getString("status"));
                list.add(p);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error finding problems", e);
        }

        return list;
    }

    @Override
    public Optional<Problem> findById(Long id) {
        String sql = "SELECT * FROM problems WHERE problem_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Problem p = new Problem();
                    p.setId(rs.getLong("problem_id"));
                    p.setTitle(rs.getString("title"));
                    p.setRawText(rs.getString("raw_text"));
                    p.setSourcePath(rs.getString("source_path"));
                    p.setSourceType(rs.getString("source_type"));
                    p.setStatus(rs.getString("status"));
                    return Optional.of(p);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error finding problem by id", e);
        }

        return Optional.empty();
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM problems WHERE problem_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Error deleting problem", e);
        }
    }
}