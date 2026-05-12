package com.yourteam.ojaitester.util;

import com.yourteam.ojaitester.dto.TestCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Công cụ tạo sample testcase để demo
 */
public class SampleTestCaseGenerator {

    public static void main(String[] args) {
        System.out.println("=== Generating Sample Testcases ===\n");

        try {
            // Sample 1: Sum
            generateSumTestCase();

            // Sample 2: Factorial
            generateFactorialTestCase();

            // Sample 3: Prime
            generatePrimeTestCase();

            System.out.println("\n✓ All sample testcases generated!");
            System.out.println("Location: submissions/testcases/");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void generateSumTestCase() throws IOException {
        TestCase tc = new TestCase(
                "5 3",
                "8",
                "Test: Cộng hai số 5 và 3"
        );
        TestCaseUtils.saveTestCase("sum", tc.getInput(), tc.getExpectedOutput(), tc.getDescription());
        System.out.println("✓ Generated: sum");
    }

    private static void generateFactorialTestCase() throws IOException {
        TestCase tc = new TestCase(
                "5",
                "120",
                "Test: Giai thừa của 5"
        );
        TestCaseUtils.saveTestCase("factorial", tc.getInput(), tc.getExpectedOutput(), tc.getDescription());
        System.out.println("✓ Generated: factorial");
    }

    private static void generatePrimeTestCase() throws IOException {
        TestCase tc = new TestCase(
                "7",
                "YES",
                "Test: Kiểm tra số nguyên tố 7"
        );
        TestCaseUtils.saveTestCase("prime", tc.getInput(), tc.getExpectedOutput(), tc.getDescription());
        System.out.println("✓ Generated: prime");
    }
}

