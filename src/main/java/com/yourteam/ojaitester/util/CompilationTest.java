package com.yourteam.ojaitester.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test compilation fix
 */
public class CompilationTest {
    
    public static void main(String[] args) {
        System.out.println("=== Compilation Fix Verification ===\n");
        
        // Test 1: Check if submissions directory exists
        System.out.println("1. Checking submissions directory...");
        File submissionsDir = new File("submissions");
        if (submissionsDir.exists() && submissionsDir.isDirectory()) {
            System.out.println("   ✓ submissions/ exists at: " + submissionsDir.getAbsolutePath());
        } else {
            System.out.println("   ✗ submissions/ does not exist, will be created on first submission");
            submissionsDir.mkdirs();
            System.out.println("   ✓ Created: " + submissionsDir.getAbsolutePath());
        }
        
        // Test 2: Check testcases directory
        System.out.println("\n2. Checking testcases directory...");
        File testcasesDir = new File("submissions/testcases");
        if (testcasesDir.exists() && testcasesDir.isDirectory()) {
            System.out.println("   ✓ testcases/ exists at: " + testcasesDir.getAbsolutePath());
        } else {
            System.out.println("   ✗ testcases/ does not exist, will be created on first submission");
            testcasesDir.mkdirs();
            System.out.println("   ✓ Created: " + testcasesDir.getAbsolutePath());
        }
        
        // Test 3: Verify g++ availability
        System.out.println("\n3. Checking g++ compiler...");
        try {
            Process process = new ProcessBuilder("g++", "--version").start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("   ✓ g++ found and working");
            } else {
                System.out.println("   ✗ g++ not responding properly");
            }
        } catch (Exception e) {
            System.out.println("   ✗ g++ not found: " + e.getMessage());
            System.out.println("   → Install MinGW-w64 from https://www.mingw-w64.org/");
        }
        
        // Test 4: Test simple compilation
        System.out.println("\n4. Testing compilation...");
        String testCode = """
                #include <iostream>
                using namespace std;
                
                int main() {
                    cout << "Hello" << endl;
                    return 0;
                }
                """;
        
        try {
            // Create test file
            Files.createDirectories(Paths.get("submissions"));
            Files.writeString(Paths.get("submissions/test_compile.cpp"), testCode);
            
            // Compile
            ProcessBuilder pb = new ProcessBuilder(
                    "g++",
                    "-o", new File("submissions/test_compile").getAbsolutePath(),
                    new File("submissions/test_compile.cpp").getAbsolutePath(),
                    "-std=c++17"
            );
            pb.directory(new File("submissions"));
            
            Process process = pb.start();
            boolean completed = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            
            if (completed && process.exitValue() == 0) {
                System.out.println("   ✓ Compilation successful!");
                
                // Check if executable was created
                File exec = new File("submissions/test_compile.exe");
                if (exec.exists()) {
                    System.out.println("   ✓ Executable created: " + exec.getAbsolutePath());
                } else {
                    File execNoExt = new File("submissions/test_compile");
                    if (execNoExt.exists()) {
                        System.out.println("   ✓ Executable created: " + execNoExt.getAbsolutePath());
                    }
                }
            } else {
                System.out.println("   ✗ Compilation failed");
            }
        } catch (Exception e) {
            System.out.println("   ✗ Error during test compilation: " + e.getMessage());
        }
        
        System.out.println("\n=== Verification Complete ===");
        System.out.println("\nYou can now:");
        System.out.println("1. Run: mvn javafx:run");
        System.out.println("2. Click Submissions");
        System.out.println("3. Submit your code!");
    }
}

