package com.yourteam.ojaitester.engine.execution;

import com.yourteam.ojaitester.dto.SubmissionResult;
import com.yourteam.ojaitester.dto.TestCase;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CppExecutionEngine {
    private static final long TIME_LIMIT_MS = 2000; // 2 giây
    private static final String WORK_DIR = "submissions";

    public CppExecutionEngine() {
        try {
            Files.createDirectories(Paths.get(WORK_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create submissions directory: " + e.getMessage());
        }
    }

    /**
     * Biên dịch code C++ thành file thực thi
     */
    public File compileCode(String cppCode, String problemName) throws IOException {
        Path workDirPath = Paths.get(WORK_DIR).toAbsolutePath();
        String safeProblemName = sanitizeProblemName(problemName);
        String submissionId = safeProblemName + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        Path sourceFile = workDirPath.resolve(submissionId + ".cpp");
        Path executablePath = workDirPath.resolve(submissionId);
        
        // Lưu source code
        Files.createDirectories(workDirPath);
        Files.writeString(sourceFile, cppCode, StandardCharsets.UTF_8);
        
        try {
            // Biên dịch bằng g++
            ProcessBuilder pb = new ProcessBuilder(
                    "g++",
                    "-o", executablePath.toAbsolutePath().toString(),
                    sourceFile.toAbsolutePath().toString(),
                    "-std=c++17"
            );
            pb.directory(workDirPath.toFile());
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            boolean completed = process.waitFor(10, TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                cleanupGeneratedFiles(sourceFile, executablePath);
                throw new IOException("Compilation timeout");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String errorOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                cleanupGeneratedFiles(sourceFile, executablePath);
                throw new IOException("Compilation failed:\n" + errorOutput);
            }
            
            // Trên Windows, thêm .exe extension
            Path finalExecPath = executablePath;
            if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")) {
                finalExecPath = Paths.get(executablePath + ".exe");
            }
            
            return finalExecPath.toFile();
        } catch (InterruptedException e) {
            cleanupGeneratedFiles(sourceFile, executablePath);
            throw new IOException("Compilation interrupted", e);
        }
    }

    /**
     * Chạy file thực thi với input và trả về output
     */
    public SubmissionResult runAndTest(File executable, TestCase testCase, String problemName) throws IOException {
        long startTime = System.currentTimeMillis();
        Path executablePath = executable.toPath();
        Path sourcePath = toSourcePath(executablePath);

        try {
            ProcessBuilder pb = new ProcessBuilder(executable.getAbsolutePath());
            pb.directory(executable.getParentFile());
            pb.redirectErrorStream(false);

            Process process = pb.start();

            // Đọc stdout/stderr song song để tránh block pipe gây treo
            CompletableFuture<String> stdoutFuture = readStreamAsync(process.getInputStream());
            CompletableFuture<String> stderrFuture = readStreamAsync(process.getErrorStream());

            // Ghi input vào stdin rồi đóng stream để chương trình biết đã hết input
            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write(testCase.getInput().getBytes(StandardCharsets.UTF_8));
                stdin.flush();
            }

            // Chờ process kết thúc hoặc timeout
            boolean completed = process.waitFor(TIME_LIMIT_MS, TimeUnit.MILLISECONDS);
            long executionTime = System.currentTimeMillis() - startTime;

            if (!completed) {
                killProcessTree(process);
                SubmissionResult result = new SubmissionResult();
                result.setVerdict(SubmissionResult.Verdict.TLE);
                result.setExecutionTime(executionTime);
                result.setExpectedOutput(testCase.getExpectedOutput());
                result.setActualOutput("");
                result.setErrorMessage("Time limit exceeded (" + TIME_LIMIT_MS + " ms)");
                return result;
            }

            int exitCode = process.exitValue();
            String output = getFutureResult(stdoutFuture);
            String errorOutput = getFutureResult(stderrFuture);

            // Kiểm tra exit code
            if (exitCode != 0) {
                SubmissionResult result = new SubmissionResult();
                result.setVerdict(SubmissionResult.Verdict.RE);
                result.setExecutionTime(executionTime);
                result.setErrorMessage(errorOutput);
                result.setExpectedOutput(testCase.getExpectedOutput());
                result.setActualOutput(output);
                return result;
            }

            // So sánh output
            String actualOutput = output.trim();
            String expectedOutput = testCase.getExpectedOutput().trim();

            SubmissionResult result = new SubmissionResult();
            result.setExecutionTime(executionTime);
            result.setExpectedOutput(expectedOutput);
            result.setActualOutput(actualOutput);

            if (compareOutput(actualOutput, expectedOutput)) {
                result.setVerdict(SubmissionResult.Verdict.AC);
            } else {
                result.setVerdict(SubmissionResult.Verdict.WA);
            }

            // Lưu testcase vào file
            saveTestCase(testCase, problemName);

            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            SubmissionResult result = new SubmissionResult();
            result.setVerdict(SubmissionResult.Verdict.TLE);
            result.setExecutionTime(System.currentTimeMillis() - startTime);
            result.setExpectedOutput(testCase.getExpectedOutput());
            result.setActualOutput("");
            result.setErrorMessage("Execution interrupted");
            return result;
        } finally {
            cleanupGeneratedFiles(sourcePath, executablePath);
        }
    }

    private Path toSourcePath(Path executablePath) {
        String fileName = executablePath.getFileName().toString();
        if (fileName.toLowerCase(Locale.ROOT).endsWith(".exe")) {
            String baseName = fileName.substring(0, fileName.length() - 4);
            return executablePath.resolveSibling(baseName + ".cpp");
        }
        return executablePath.resolveSibling(fileName + ".cpp");
    }

    private void cleanupGeneratedFiles(Path sourcePath, Path executableBasePath) {
        cleanupOneFile(sourcePath);
        cleanupOneFile(executableBasePath);
        cleanupOneFile(Paths.get(executableBasePath.toString() + ".exe"));
    }

    private void cleanupOneFile(Path filePath) {
        if (filePath == null || isUnderTestcaseDirectory(filePath)) {
            return;
        }
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return;
        }

        for (int i = 0; i < 5; i++) {
            try {
                Files.deleteIfExists(filePath);
                return;
            } catch (IOException e) {
                try {
                    Thread.sleep(80L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private boolean isUnderTestcaseDirectory(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        Path testcaseDir = Paths.get(WORK_DIR, "testcases").toAbsolutePath().normalize();
        return normalized.startsWith(testcaseDir);
    }

    private CompletableFuture<String> readStreamAsync(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            try (inputStream; ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                inputStream.transferTo(buffer);
                return buffer.toString(StandardCharsets.UTF_8);
            } catch (IOException e) {
                return "";
            }
        });
    }

    private String getFutureResult(CompletableFuture<String> future) {
        try {
            return future.get(500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return "";
        }
    }

    private void killProcessTree(Process process) {
        ProcessHandle handle = process.toHandle();
        handle.descendants().forEach(child -> {
            try {
                child.destroyForcibly();
            } catch (Exception ignored) {
            }
        });
        process.destroyForcibly();
        try {
            process.waitFor(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String sanitizeProblemName(String problemName) {
        if (problemName == null || problemName.isBlank()) {
            return "submission";
        }
        return problemName.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    /**
     * So sánh output (bỏ qua whitespace ở cuối)
     */
    private boolean compareOutput(String actual, String expected) {
        String[] actualLines = actual.split("\\n");
        String[] expectedLines = expected.split("\\n");
        
        if (actualLines.length != expectedLines.length) {
            return false;
        }
        
        for (int i = 0; i < actualLines.length; i++) {
            if (!actualLines[i].trim().equals(expectedLines[i].trim())) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Lưu testcase vào file riêng cho bài
     */
    private void saveTestCase(TestCase testCase, String problemName) throws IOException {
        String testcaseDir = "submissions" + File.separator + "testcases" + File.separator + problemName;
        Files.createDirectories(Paths.get(testcaseDir));
        
        Files.writeString(
                Paths.get(testcaseDir, "input.txt"),
                testCase.getInput(),
                StandardCharsets.UTF_8
        );
        
        Files.writeString(
                Paths.get(testcaseDir, "output.txt"),
                testCase.getExpectedOutput(),
                StandardCharsets.UTF_8
        );
        
        if (testCase.getDescription() != null) {
            Files.writeString(
                    Paths.get(testcaseDir, "description.txt"),
                    testCase.getDescription(),
                    StandardCharsets.UTF_8
            );
        }
    }

    /**
     * Lấy đường dẫn file thực thi
     */
    public String getExecutablePath(String problemName) {
        Path workDirPath = Paths.get(WORK_DIR).toAbsolutePath();
        Path exec = workDirPath.resolve(sanitizeProblemName(problemName));
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")) {
            return exec.toString() + ".exe";
        }
        return exec.toString();
    }
}






