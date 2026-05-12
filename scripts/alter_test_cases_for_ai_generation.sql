USE OJ_AI_TESTER;
GO

IF COL_LENGTH('test_cases', 'created_at') IS NULL
BEGIN
    ALTER TABLE test_cases
    ADD created_at DATETIME DEFAULT GETDATE();
END;
GO

IF COL_LENGTH('test_cases', 'updated_at') IS NULL
BEGIN
    ALTER TABLE test_cases
    ADD updated_at DATETIME DEFAULT GETDATE();
END;
GO

IF COL_LENGTH('test_cases', 'purpose') IS NOT NULL
BEGIN
    ALTER TABLE test_cases
    ALTER COLUMN purpose NVARCHAR(MAX);
END;
GO

IF COL_LENGTH('test_cases', 'category') IS NOT NULL
BEGIN
    ALTER TABLE test_cases
    ALTER COLUMN category NVARCHAR(100);
END;
GO
