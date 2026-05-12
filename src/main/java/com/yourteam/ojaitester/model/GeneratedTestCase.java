package com.yourteam.ojaitester.model;

public class GeneratedTestCase extends TestCase {

    public GeneratedTestCase() {
    }

    public GeneratedTestCase(Long problemId, String inputData, String expectedOutput,
                             String category, String purpose) {
        setProblemId(problemId);
        setInputData(inputData);
        setExpectedOutput(expectedOutput);
        setCategory(category);
        setPurpose(purpose);
        setGeneratedBy("GEMINI");
    }
}
