package com.yourteam.ojaitester.repository.impl;

import com.yourteam.ojaitester.config.DatabaseConfig;
import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.repository.ProblemRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProblemRepositoryImpl implements ProblemRepository {

    private Problem mapProblem(ResultSet rs) throws SQLException {
        Problem p = new Problem();
        p.setId(rs.getLong("problem_id"));
        p.setTitle(rs.getString("title"));
        p.setRawText(rs.getString("raw_text"));
        p.setSourcePath(rs.getString("source_path"));
        p.setSourceType(rs.getString("source_type"));
        p.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            p.setCreatedAt(createdAt.toLocalDateTime());
        }

        return p;
    }

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
                list.add(mapProblem(rs));
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
                    return Optional.of(mapProblem(rs));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error finding problem by id", e);
        }

        return Optional.empty();
    }

    @Override
    public void deleteById(Long id) {
        String deleteExecutionResultsBySubmission = "DELETE FROM execution_results WHERE submission_id IN (SELECT submission_id FROM submissions WHERE problem_id = ?)";
        String deleteExecutionResultsByTestcase = "DELETE FROM execution_results WHERE testcase_id IN (SELECT testcase_id FROM test_cases WHERE problem_id = ?)";
        String deleteSubmissions = "DELETE FROM submissions WHERE problem_id = ?";
        String deleteTestCases = "DELETE FROM test_cases WHERE problem_id = ?";
        String deleteParsedProblems = "DELETE FROM parsed_problems WHERE problem_id = ?";
        String deleteProblem = "DELETE FROM problems WHERE problem_id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(deleteExecutionResultsBySubmission);
                 PreparedStatement ps2 = conn.prepareStatement(deleteExecutionResultsByTestcase);
                 PreparedStatement ps3 = conn.prepareStatement(deleteSubmissions);
                 PreparedStatement ps4 = conn.prepareStatement(deleteTestCases);
                 PreparedStatement ps5 = conn.prepareStatement(deleteParsedProblems);
                 PreparedStatement ps6 = conn.prepareStatement(deleteProblem)) {

                ps1.setLong(1, id);
                ps1.executeUpdate();

                ps2.setLong(1, id);
                ps2.executeUpdate();

                ps3.setLong(1, id);
                ps3.executeUpdate();

                ps4.setLong(1, id);
                ps4.executeUpdate();

                ps5.setLong(1, id);
                ps5.executeUpdate();

                ps6.setLong(1, id);
                ps6.executeUpdate();

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error deleting problem", e);
        }
    }
}