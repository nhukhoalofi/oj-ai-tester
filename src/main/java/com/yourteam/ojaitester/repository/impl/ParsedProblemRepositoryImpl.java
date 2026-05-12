package com.yourteam.ojaitester.repository.impl;

import com.yourteam.ojaitester.config.DatabaseConfig;
import com.yourteam.ojaitester.model.ParsedProblem;
import com.yourteam.ojaitester.repository.ParsedProblemRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

public class ParsedProblemRepositoryImpl implements ParsedProblemRepository {

    @Override
    public ParsedProblem saveOrUpdate(ParsedProblem parsedProblem) {
        if (parsedProblem == null || parsedProblem.getProblemId() == null) {
            throw new IllegalArgumentException("Parsed problem and problem id must not be null.");
        }

        try {
            ensureSchema();
            Optional<ParsedProblem> existing = findByProblemId(parsedProblem.getProblemId());
            return existing.isPresent() ? update(existing.get().getId(), parsedProblem) : insert(parsedProblem);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cannot save parsed problem")) {
                throw e;
            }
            throw new RuntimeException("Cannot save parsed problem to database: " + rootMessage(e), e);
        }
    }

    @Override
    public Optional<ParsedProblem> findByProblemId(Long problemId) {
        ensureSchema();
        String sql = """
                SELECT id, problem_id, title, statement, input_format, output_format,
                       constraints_text, tags, summary, created_at, updated_at
                FROM parsed_problems
                WHERE problem_id = ?
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, problemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapParsedProblem(rs));
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Cannot find parsed problem by problem id", e);
        }
    }

    private void ensureSchema() {
        String sql = """
                IF OBJECT_ID('parsed_problems', 'U') IS NULL
                BEGIN
                    CREATE TABLE parsed_problems (
                        id BIGINT IDENTITY(1,1) PRIMARY KEY,
                        problem_id BIGINT NOT NULL,
                        title NVARCHAR(500),
                        statement NVARCHAR(MAX),
                        input_format NVARCHAR(MAX),
                        output_format NVARCHAR(MAX),
                        constraints_text NVARCHAR(MAX),
                        tags NVARCHAR(MAX),
                        summary NVARCHAR(MAX),
                        created_at DATETIME DEFAULT GETDATE(),
                        updated_at DATETIME DEFAULT GETDATE(),
                        CONSTRAINT fk_parsed_problem_problem
                            FOREIGN KEY (problem_id) REFERENCES problems(problem_id)
                    );
                END;

                IF COL_LENGTH('parsed_problems', 'id') IS NULL
                   AND COL_LENGTH('parsed_problems', 'parsed_id') IS NOT NULL
                BEGIN
                    EXEC sp_rename 'parsed_problems.parsed_id', 'id', 'COLUMN';
                END;

                IF COL_LENGTH('parsed_problems', 'title') IS NULL
                BEGIN
                    ALTER TABLE parsed_problems ADD title NVARCHAR(500);
                END;

                IF COL_LENGTH('parsed_problems', 'summary') IS NULL
                BEGIN
                    IF COL_LENGTH('parsed_problems', 'ai_summary') IS NOT NULL
                        EXEC sp_rename 'parsed_problems.ai_summary', 'summary', 'COLUMN';
                    ELSE
                        ALTER TABLE parsed_problems ADD summary NVARCHAR(MAX);
                END;

                IF COL_LENGTH('parsed_problems', 'created_at') IS NULL
                BEGIN
                    ALTER TABLE parsed_problems ADD created_at DATETIME DEFAULT GETDATE();
                END;

                IF COL_LENGTH('parsed_problems', 'updated_at') IS NULL
                BEGIN
                    ALTER TABLE parsed_problems ADD updated_at DATETIME DEFAULT GETDATE();
                END;

                IF COL_LENGTH('parsed_problems', 'tags') IS NOT NULL
                BEGIN
                    ALTER TABLE parsed_problems ALTER COLUMN tags NVARCHAR(MAX);
                END;
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement statement = conn.createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException("Cannot prepare parsed_problems schema.", e);
        }
    }

    private ParsedProblem insert(ParsedProblem parsedProblem) {
        String sql = """
                INSERT INTO parsed_problems
                    (problem_id, title, statement, input_format, output_format,
                     constraints_text, tags, summary, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindEditableFields(ps, parsedProblem);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    parsedProblem.setId(rs.getLong(1));
                }
            }
            return parsedProblem;
        } catch (Exception e) {
            throw new RuntimeException("Cannot save parsed problem to database: " + rootMessage(e), e);
        }
    }

    private ParsedProblem update(Long id, ParsedProblem parsedProblem) {
        String sql = """
                UPDATE parsed_problems
                SET problem_id = ?,
                    title = ?,
                    statement = ?,
                    input_format = ?,
                    output_format = ?,
                    constraints_text = ?,
                    tags = ?,
                    summary = ?,
                    updated_at = GETDATE()
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindEditableFields(ps, parsedProblem);
            ps.setLong(9, id);
            ps.executeUpdate();
            parsedProblem.setId(id);
            return parsedProblem;
        } catch (Exception e) {
            throw new RuntimeException("Cannot save parsed problem to database: " + rootMessage(e), e);
        }
    }

    private void bindEditableFields(PreparedStatement ps, ParsedProblem parsedProblem) throws Exception {
        ps.setLong(1, parsedProblem.getProblemId());
        ps.setString(2, parsedProblem.getTitle());
        ps.setString(3, parsedProblem.getStatement());
        ps.setString(4, parsedProblem.getInputFormat());
        ps.setString(5, parsedProblem.getOutputFormat());
        ps.setString(6, parsedProblem.getConstraintsText());
        ps.setString(7, parsedProblem.getTags());
        ps.setString(8, parsedProblem.getSummary());
    }

    private ParsedProblem mapParsedProblem(ResultSet rs) throws Exception {
        ParsedProblem parsedProblem = new ParsedProblem();
        parsedProblem.setId(rs.getLong("id"));
        parsedProblem.setProblemId(rs.getLong("problem_id"));
        parsedProblem.setTitle(rs.getString("title"));
        parsedProblem.setStatement(rs.getString("statement"));
        parsedProblem.setInputFormat(rs.getString("input_format"));
        parsedProblem.setOutputFormat(rs.getString("output_format"));
        parsedProblem.setConstraintsText(rs.getString("constraints_text"));
        parsedProblem.setTags(rs.getString("tags"));
        parsedProblem.setSummary(rs.getString("summary"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            parsedProblem.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            parsedProblem.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return parsedProblem;
    }

    private String rootMessage(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor.getCause() != null) {
            cursor = cursor.getCause();
        }
        return cursor.getMessage() != null ? cursor.getMessage() : cursor.toString();
    }
}
