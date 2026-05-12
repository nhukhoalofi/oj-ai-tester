package com.yourteam.ojaitester.util;

import com.yourteam.ojaitester.dto.TestCase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestCaseUtils {
    private static final String TESTCASE_DIR = "submissions/testcases";

    /**
     * Lưu testcase từ bộ nhớ
     */
    public static void saveTestCase(String problemName, String input, String expectedOutput, String description) throws IOException {
        String testcaseDir = TESTCASE_DIR + "/" + problemName;
        Files.createDirectories(Paths.get(testcaseDir));

        Files.writeString(
                Paths.get(testcaseDir, "input.txt"),
                input,
                StandardCharsets.UTF_8
        );

        Files.writeString(
                Paths.get(testcaseDir, "output.txt"),
                expectedOutput,
                StandardCharsets.UTF_8
        );

        if (description != null && !description.isEmpty()) {
            Files.writeString(
                    Paths.get(testcaseDir, "description.txt"),
                    description,
                    StandardCharsets.UTF_8
            );
        }
    }

    /**
     * Load testcase từ file
     */
    public static TestCase loadTestCase(String problemName) throws IOException {
        String testcaseDir = TESTCASE_DIR + "/" + problemName;
        Path inputPath = Paths.get(testcaseDir, "input.txt");
        Path outputPath = Paths.get(testcaseDir, "output.txt");
        Path descPath = Paths.get(testcaseDir, "description.txt");

        if (!Files.exists(inputPath) || !Files.exists(outputPath)) {
            throw new IOException("Test case not found for problem: " + problemName);
        }

        String input = Files.readString(inputPath, StandardCharsets.UTF_8);
        String expectedOutput = Files.readString(outputPath, StandardCharsets.UTF_8);
        String description = Files.exists(descPath) ? Files.readString(descPath, StandardCharsets.UTF_8) : null;

        return new TestCase(input, expectedOutput, description);
    }

    /**
     * Kiểm tra xem testcase có tồn tại không
     */
    public static boolean testCaseExists(String problemName) {
        String testcaseDir = TESTCASE_DIR + "/" + problemName;
        Path inputPath = Paths.get(testcaseDir, "input.txt");
        Path outputPath = Paths.get(testcaseDir, "output.txt");
        return Files.exists(inputPath) && Files.exists(outputPath);
    }
}

