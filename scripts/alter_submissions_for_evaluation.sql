USE OJ_AI_TESTER;
GO

IF COL_LENGTH('submissions', 'updated_at') IS NULL
BEGIN
    ALTER TABLE submissions ADD updated_at DATETIME DEFAULT GETDATE();
END;
GO

IF COL_LENGTH('submissions', 'note') IS NULL
BEGIN
    ALTER TABLE submissions ADD note NVARCHAR(MAX);
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'ck_submissions_type'
)
BEGIN
    ALTER TABLE submissions
    ADD CONSTRAINT ck_submissions_type
        CHECK (submission_type IN ('AC', 'WA', 'TLE'));
END;
GO
