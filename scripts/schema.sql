CREATE DATABASE OJ_AI_TESTER;
GO

USE OJ_AI_TESTER;
GO

CREATE TABLE problems (
    problem_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    raw_text NVARCHAR(MAX),
    source_path NVARCHAR(500),
    source_type NVARCHAR(50),
    status NVARCHAR(50),
    created_at DATETIME DEFAULT GETDATE()
);

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
    CONSTRAINT uq_parsed_problems_problem_id UNIQUE (problem_id),
    CONSTRAINT fk_parsed_problem_problem
        FOREIGN KEY (problem_id) REFERENCES problems(problem_id)
);

CREATE TABLE test_cases (
    testcase_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    category NVARCHAR(50),
    input_data NVARCHAR(MAX),
    expected_output NVARCHAR(MAX),
    purpose NVARCHAR(MAX),
    strength_score INT,
    generated_by NVARCHAR(50),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id)
);

CREATE TABLE submissions (
    submission_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    name NVARCHAR(255),
    submission_type NVARCHAR(50),
    language NVARCHAR(50),
    source_code NVARCHAR(MAX),
    note NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id)
);

CREATE TABLE execution_results (
    result_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    submission_id BIGINT NULL,
    problem_id BIGINT NOT NULL,
    testcase_id BIGINT NOT NULL,
    status NVARCHAR(50),
    execution_time_ms INT,
    memory_kb INT,
    actual_output NVARCHAR(MAX),
    expected_output NVARCHAR(MAX),
    error_message NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id),
    FOREIGN KEY (submission_id) REFERENCES submissions(submission_id),
    FOREIGN KEY (testcase_id) REFERENCES test_cases(testcase_id)
);
