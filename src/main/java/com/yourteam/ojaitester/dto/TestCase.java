package com.yourteam.ojaitester.dto;

public class TestCase {
    private String input;
    private String expectedOutput;
    private String description;

    public TestCase() {
    }

    public TestCase(String input, String expectedOutput) {
        this.input = input;
        this.expectedOutput = expectedOutput;
    }

    public TestCase(String input, String expectedOutput, String description) {
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.description = description;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

