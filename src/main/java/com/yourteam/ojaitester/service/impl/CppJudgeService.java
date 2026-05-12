package com.yourteam.ojaitester.service.impl;

import com.yourteam.ojaitester.model.ExecutionResult;
import com.yourteam.ojaitester.model.Submission;
import com.yourteam.ojaitester.model.SubmissionRunReport;
import com.yourteam.ojaitester.model.TestCase;
import com.yourteam.ojaitester.service.CheckerService;
import com.yourteam.ojaitester.service.CheckerType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class CppJudgeService {

    private static final int DEFAULT_TIMEOUT_MS = 2000;
    private final CheckerService checkerService = new CheckerService();

    // Educational runner only: submitted code is executed directly, with timeout but without an OS sandbox.
    public SubmissionRunReport run(Submission submission, List<TestCase> testCases) {
        return run(submission, testCases, DEFAULT_TIMEOUT_MS, null);
    }

    public SubmissionRunReport run(Submission submission, List<TestCase> testCases, int timeoutMs) {
        return run(submission, testCases, timeoutMs, null);
    }

    public SubmissionRunReport run(Submission submission, List<TestCase> testCases, int timeoutMs, Consumer<String> progressCallback) {
        SubmissionRunReport report = new SubmissionRunReport();
        report.setSubmissionId(submission.getId());
        report.setSubmissionName(submission.getName());
        report.setTotalTestcases(testCases.size());

        Path tempDir = null;
        try {
            try {
                tempDir = Files.createTempDirectory("ojaitester-submission-");
            } catch (IOException e) {
                throw new RuntimeException("Cannot create temporary source file.", e);
            }
            Path sourceFile = tempDir.resolve("main.cpp");
            Path executableFile = isWindows() ? tempDir.resolve("main.exe") : tempDir.resolve("main");
            try {
                Files.writeString(sourceFile, submission.getSourceCode(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create temporary source file.", e);
            }

            CompileResult compileResult = compileCpp(sourceFile, executableFile);
            if (!compileResult.success) {
                if (testCases.isEmpty()) {
                    report.setCompileErrorCount(1);
                } else {
                    for (TestCase testCase : testCases) {
                        report.getResults().add(buildResult(submission, testCase, "CE", null, compileResult.errorMessage, null));
                    }
                    report.setCompileErrorCount(testCases.size());
                }
                report.setOverallStatus("CE");
                report.setSummaryMessage(compileResult.errorMessage);
                return report;
            }

            if (testCases.isEmpty()) {
                report.setOverallStatus("NO_TESTCASE");
                report.setSummaryMessage("No test cases found for this problem.");
                return report;
            }

            for (int i = 0; i < testCases.size(); i++) {
                notifyProgress(progressCallback, "Running " + (i + 1) + "/" + testCases.size() + " testcases...");
                TestCase testCase = testCases.get(i);
                ExecutionResult result = runSingleTestCase(submission, executableFile, testCase, timeoutMs);
                report.getResults().add(result);
                report.setTotalRuntimeMs(report.getTotalRuntimeMs() + safeInt(result.getExecutionTimeMs()));
                switch (result.getStatus()) {
                    case "AC" -> report.setPassedCount(report.getPassedCount() + 1);
                    case "WA" -> report.setWrongAnswerCount(report.getWrongAnswerCount() + 1);
                    case "TLE" -> report.setTleCount(report.getTleCount() + 1);
                    case "RE" -> report.setRuntimeErrorCount(report.getRuntimeErrorCount() + 1);
                    case "CE" -> report.setCompileErrorCount(report.getCompileErrorCount() + 1);
                    default -> {
                    }
                }
            }

            report.setOverallStatus(determineOverallStatus(report));
            report.setSummaryMessage(buildSummary(report));
            return report;
        } catch (IOException e) {
            throw new RuntimeException("Failed to prepare C++ run environment: " + e.getMessage(), e);
        } finally {
            if (tempDir != null) {
                deleteRecursively(tempDir);
            }
        }
    }

    private ExecutionResult runSingleTestCase(Submission submission, Path executableFile, TestCase testCase, int timeoutMs) {
        long startNanos = System.nanoTime();
        Process process;
        String output = "";
        String error;
        try {
            ProcessBuilder builder = new ProcessBuilder(executableFile.toAbsolutePath().toString());
            process = builder.start();
            OutputCapture outputCapture = new OutputCapture(process.getInputStream());
            OutputCapture errorCapture = new OutputCapture(process.getErrorStream());
            outputCapture.start();
            errorCapture.start();

            try (OutputStream stdin = process.getOutputStream()) {
                String inputData = testCase.getInputData() != null ? testCase.getInputData() : "";
                stdin.write(inputData.getBytes(StandardCharsets.UTF_8));
                stdin.flush();
            }

            boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                process.waitFor();
                return buildTimedResult(submission, testCase, elapsedMs(startNanos));
            }

            output = outputCapture.await();
            error = errorCapture.await();
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String message = !error.isBlank() ? error.trim() : "Process exited with code " + exitCode;
                return buildResult(submission, testCase, "RE", elapsedMs(startNanos), message, normalizeOutput(output));
            }

            String actual = normalizeOutput(output);
            String expected = testCase.getExpectedOutput();
            if (expected == null || expected.isBlank()) {
                return buildResult(submission, testCase, "WA", elapsedMs(startNanos), "Expected output is empty", actual);
            }

            String status = checkerService.isAccepted(actual, expected, CheckerType.TOKEN) ? "AC" : "WA";
            return buildResult(submission, testCase, status, elapsedMs(startNanos), null, actual);
        } catch (IOException e) {
            error = "Cannot execute compiled program.";
            return buildResult(submission, testCase, "RE", elapsedMs(startNanos), error, normalizeOutput(output));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            error = e.getMessage();
            return buildResult(submission, testCase, "RE", elapsedMs(startNanos), error, normalizeOutput(output));
        }
    }

    private CompileResult compileCpp(Path sourceFile, Path executableFile) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("g++");
        command.add("-std=c++17");
        command.add("-O2");
        command.add("-pipe");
        command.add(sourceFile.toAbsolutePath().toString());
        command.add("-o");
        command.add(executableFile.toAbsolutePath().toString());

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process;
        try {
            process = builder.start();
        } catch (IOException ex) {
            throw new IOException("g++ compiler not found. Please install MinGW or configure PATH.", ex);
        }

        try {
            OutputCapture outputCapture = new OutputCapture(process.getInputStream());
            outputCapture.start();
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                process.waitFor();
                return new CompileResult(false, "Compilation timed out.");
            }
            int exitCode = process.exitValue();
            String output = outputCapture.await();
            if (exitCode != 0) {
                return new CompileResult(false, output.isBlank() ? "Compilation failed." : output.trim());
            }
            return new CompileResult(true, "");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new CompileResult(false, "Compilation interrupted: " + e.getMessage());
        }
    }

    private ExecutionResult buildTimedResult(Submission submission, TestCase testCase, int executionTimeMs) {
        return buildResult(submission, testCase, "TLE", executionTimeMs, "Time limit exceeded", null);
    }

    private ExecutionResult buildResult(Submission submission, TestCase testCase, String status, Integer executionTimeMs, String errorMessage, String actualOutput) {
        ExecutionResult result = new ExecutionResult();
        result.setSubmissionId(submission.getId());
        result.setProblemId(submission.getProblemId());
        result.setTestcaseId(testCase.getId());
        result.setCategory(testCase.getCategory());
        result.setInputData(testCase.getInputData());
        result.setExpectedOutput(testCase.getExpectedOutput());
        result.setStatus(status);
        result.setExecutionTimeMs(executionTimeMs);
        result.setMemoryKb(null);
        result.setActualOutput(actualOutput);
        result.setErrorMessage(errorMessage);
        return result;
    }

    private void notifyProgress(Consumer<String> progressCallback, String message) {
        if (progressCallback != null) {
            progressCallback.accept(message);
        }
    }

    private int elapsedMs(long startNanos) {
        return (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    private String buildSummary(SubmissionRunReport report) {
        return "Total: " + report.getTotalTestcases()
                + ", AC: " + report.getPassedCount()
                + ", WA: " + report.getWrongAnswerCount()
                + ", TLE: " + report.getTleCount()
                + ", RE: " + report.getRuntimeErrorCount()
                + ", CE: " + report.getCompileErrorCount()
                + " | Overall: " + report.getOverallStatus();
    }

    private String determineOverallStatus(SubmissionRunReport report) {
        if (report.getCompileErrorCount() > 0) {
            return "CE";
        }
        if (report.getRuntimeErrorCount() > 0) {
            return "RE";
        }
        if (report.getTleCount() > 0) {
            return "TLE";
        }
        if (report.getWrongAnswerCount() > 0) {
            return "WA";
        }
        return "AC";
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    private String normalizeOutput(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private static final class OutputCapture implements Runnable {
        private final InputStream inputStream;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final Thread thread;

        private OutputCapture(InputStream inputStream) {
            this.inputStream = inputStream;
            this.thread = new Thread(this, "cpp-output-capture");
            this.thread.setDaemon(true);
        }

        private void start() {
            thread.start();
        }

        private String await() throws InterruptedException {
            thread.join();
            return buffer.toString(StandardCharsets.UTF_8);
        }

        @Override
        public void run() {
            byte[] bufferArray = new byte[4096];
            int read;
            try (InputStream in = inputStream) {
                while ((read = in.read(bufferArray)) != -1) {
                    buffer.write(bufferArray, 0, read);
                }
            } catch (IOException ignored) {
            }
        }
    }

    private void deleteRecursively(Path path) {
        try {
            if (!Files.exists(path)) {
                return;
            }
            try (Stream<Path> paths = Files.walk(path)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException ignored) {
                            }
                        });
            }
        } catch (IOException ignored) {
        }
    }

    private boolean isWindows() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        return osName.contains("win");
    }

    private static final class CompileResult {
        private final boolean success;
        private final String errorMessage;

        private CompileResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
    }
}

