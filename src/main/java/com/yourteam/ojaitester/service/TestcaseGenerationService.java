package com.yourteam.ojaitester.service;

import com.yourteam.ojaitester.model.GeneratedTestCase;
import com.yourteam.ojaitester.model.Problem;

import java.util.List;

public interface TestcaseGenerationService {
    List<GeneratedTestCase> generateTestcases(Problem problem);
}
