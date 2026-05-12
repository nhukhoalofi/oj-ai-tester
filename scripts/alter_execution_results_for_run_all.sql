USE OJ_AI_TESTER;
GO

IF COL_LENGTH('execution_results', 'problem_id') IS NULL
BEGIN
    ALTER TABLE execution_results ADD problem_id BIGINT NULL;
END;
GO

IF COL_LENGTH('execution_results', 'submission_id') IS NOT NULL
BEGIN
    ALTER TABLE execution_results ALTER COLUMN submission_id BIGINT NULL;
END;
GO

IF COL_LENGTH('execution_results', 'expected_output') IS NULL
BEGIN
    ALTER TABLE execution_results ADD expected_output NVARCHAR(MAX);
END;
GO

IF COL_LENGTH('execution_results', 'created_at') IS NULL
BEGIN
    ALTER TABLE execution_results ADD created_at DATETIME DEFAULT GETDATE();
END;
GO

UPDATE er
SET problem_id = s.problem_id
FROM execution_results er
JOIN submissions s ON s.submission_id = er.submission_id
WHERE er.problem_id IS NULL;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'fk_execution_results_problem'
)
BEGIN
    ALTER TABLE execution_results
    ADD CONSTRAINT fk_execution_results_problem
        FOREIGN KEY (problem_id) REFERENCES problems(problem_id);
END;
GO
