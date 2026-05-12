package com.yourteam.ojaitester.service;

import com.yourteam.ojaitester.dto.SubmissionResult;
import com.yourteam.ojaitester.dto.TestCase;

public interface SubmissionService {
    /**
     * Nộp bài và kiểm tra
     */
    SubmissionResult submitCode(String cppCode, TestCase testCase, String problemName) throws Exception;
}

