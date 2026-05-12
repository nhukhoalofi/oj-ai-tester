package com.yourteam.ojaitester.util;

import com.yourteam.ojaitester.dto.SubmissionResult;
import com.yourteam.ojaitester.dto.TestCase;
import com.yourteam.ojaitester.service.SubmissionService;
import com.yourteam.ojaitester.service.impl.SubmissionServiceImpl;

/**
 * Ví dụ test các bài toán đơn giản
 */
public class SampleSubmissionTest {

    public static void main(String[] args) {
        SubmissionService service = new SubmissionServiceImpl();

        // Test 1: Sum của 2 số
        testSumProgram(service);

        // Test 2: Factorial
        testFactorial(service);

        // Test 3: Prime check
        testPrimeCheck(service);
    }

    private static void testSumProgram(SubmissionService service) {
        System.out.println("\n=== Test 1: Sum of 2 Numbers ===");
        
        String code = """
                #include <iostream>
                using namespace std;
                
                int main() {
                    int a, b;
                    cin >> a >> b;
                    cout << a + b << endl;
                    return 0;
                }
                """;

        TestCase testCase = new TestCase(
                "5 3",
                "8",
                "Test sum of 5 and 3"
        );

        try {
            SubmissionResult result = service.submitCode(code, testCase, "sum");
            printResult(result);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void testFactorial(SubmissionService service) {
        System.out.println("\n=== Test 2: Factorial ===");
        
        String code = """
                #include <iostream>
                using namespace std;
                
                int main() {
                    int n;
                    cin >> n;
                    int result = 1;
                    for (int i = 2; i <= n; i++) {
                        result *= i;
                    }
                    cout << result << endl;
                    return 0;
                }
                """;

        TestCase testCase = new TestCase(
                "5",
                "120",
                "Test factorial of 5"
        );

        try {
            SubmissionResult result = service.submitCode(code, testCase, "factorial");
            printResult(result);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void testPrimeCheck(SubmissionService service) {
        System.out.println("\n=== Test 3: Prime Check ===");
        
        String code = """
                #include <iostream>
                using namespace std;
                
                int main() {
                    int n;
                    cin >> n;
                    
                    if (n < 2) {
                        cout << "NO" << endl;
                        return 0;
                    }
                    
                    for (int i = 2; i * i <= n; i++) {
                        if (n % i == 0) {
                            cout << "NO" << endl;
                            return 0;
                        }
                    }
                    
                    cout << "YES" << endl;
                    return 0;
                }
                """;

        TestCase testCase = new TestCase(
                "7",
                "YES",
                "Test if 7 is prime"
        );

        try {
            SubmissionResult result = service.submitCode(code, testCase, "prime");
            printResult(result);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void printResult(SubmissionResult result) {
        System.out.println("Verdict: " + result.getVerdict().getLabel());
        System.out.println("Time: " + result.getExecutionTime() + " ms");
        System.out.println("Expected: " + result.getExpectedOutput().replace("\n", "\\n"));
        System.out.println("Actual: " + result.getActualOutput().replace("\n", "\\n"));
        if (result.getErrorMessage() != null && !result.getErrorMessage().isEmpty()) {
            System.out.println("Error: " + result.getErrorMessage());
        }
    }
}

