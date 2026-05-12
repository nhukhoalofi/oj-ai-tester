package com.yourteam.ojaitester.service.impl;

import com.yourteam.ojaitester.dto.SubmissionResult;
import com.yourteam.ojaitester.dto.TestCase;
import com.yourteam.ojaitester.engine.execution.CppExecutionEngine;
import com.yourteam.ojaitester.service.SubmissionService;

import java.io.File;

public class SubmissionServiceImpl implements SubmissionService {
    private final CppExecutionEngine executionEngine;

    public SubmissionServiceImpl() {
        this.executionEngine = new CppExecutionEngine();
    }

    @Override
    public SubmissionResult submitCode(String cppCode, TestCase testCase, String problemName) throws Exception {
        try {
            // 1. Biên dịch code
            File executable = executionEngine.compileCode(cppCode, problemName);
            
            // 2. Chạy và kiểm tra
            return executionEngine.runAndTest(executable, testCase, problemName);
        } catch (Exception e) {
            SubmissionResult result = new SubmissionResult();
            result.setVerdict(SubmissionResult.Verdict.CE);
            result.setErrorMessage(e.getMessage());
            result.setExpectedOutput(testCase.getExpectedOutput());
            result.setActualOutput("");
            return result;
        }
    }
}

