USE OJ_AI_TESTER;
GO

IF COL_LENGTH('parsed_problems', 'id') IS NULL
BEGIN
    EXEC sp_rename 'parsed_problems.parsed_id', 'id', 'COLUMN';
END
GO

IF COL_LENGTH('parsed_problems', 'title') IS NULL
BEGIN
    ALTER TABLE parsed_problems ADD title NVARCHAR(500);
END
GO

IF COL_LENGTH('parsed_problems', 'summary') IS NULL
BEGIN
    IF COL_LENGTH('parsed_problems', 'ai_summary') IS NOT NULL
        EXEC sp_rename 'parsed_problems.ai_summary', 'summary', 'COLUMN';
    ELSE
        ALTER TABLE parsed_problems ADD summary NVARCHAR(MAX);
END
GO

IF COL_LENGTH('parsed_problems', 'created_at') IS NULL
BEGIN
    ALTER TABLE parsed_problems ADD created_at DATETIME DEFAULT GETDATE();
END
GO

IF COL_LENGTH('parsed_problems', 'updated_at') IS NULL
BEGIN
    ALTER TABLE parsed_problems ADD updated_at DATETIME DEFAULT GETDATE();
END
GO

IF COL_LENGTH('parsed_problems', 'tags') IS NOT NULL
BEGIN
    ALTER TABLE parsed_problems ALTER COLUMN tags NVARCHAR(MAX);
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'uq_parsed_problems_problem_id'
      AND object_id = OBJECT_ID('parsed_problems')
)
BEGIN
    ALTER TABLE parsed_problems
    ADD CONSTRAINT uq_parsed_problems_problem_id UNIQUE (problem_id);
END
GO
