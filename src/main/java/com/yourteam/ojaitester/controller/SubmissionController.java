package com.yourteam.ojaitester.controller;

import com.yourteam.ojaitester.dto.SubmissionResult;
import com.yourteam.ojaitester.dto.TestCase;
import com.yourteam.ojaitester.service.SubmissionService;
import com.yourteam.ojaitester.service.impl.SubmissionServiceImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SubmissionController {
    @FXML
    private TextField problemNameField;
    @FXML
    private TextArea codeArea;
    @FXML
    private TextArea inputArea;
    @FXML
    private TextArea expectedOutputArea;
    @FXML
    private Button submitButton;
    @FXML
    private Button clearButton;
    @FXML
    private Label verdictLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private TextArea actualOutputArea;
    @FXML
    private TextArea errorMessageArea;

    private final SubmissionService submissionService = new SubmissionServiceImpl();

    @FXML
    public void initialize() {
        submitButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30;");
        clearButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-padding: 10 30;");
        clearResult();
    }

    @FXML
    private void handleSubmit() {
        String problemName = problemNameField.getText().trim();
        String code = codeArea.getText().trim();
        String input = inputArea.getText().trim();
        String expectedOutput = expectedOutputArea.getText().trim();

        // Validation
        if (problemName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter problem name");
            return;
        }

        if (code.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter C++ code");
            return;
        }

        if (input.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter test input");
            return;
        }

        if (expectedOutput.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter expected output");
            return;
        }

        // Disable submit button during execution
        submitButton.setDisable(true);
        verdictLabel.setText("Processing...");
        verdictLabel.setStyle("-fx-text-fill: #f59e0b;");
        errorMessageArea.clear();
        actualOutputArea.clear();

        // Run in background thread
        new Thread(() -> {
            try {
                TestCase testCase = new TestCase(input, expectedOutput);
                SubmissionResult result = submissionService.submitCode(code, testCase, problemName);

                // Update UI in JavaFX thread
                Platform.runLater(() -> {
                    displayResult(result);
                    submitButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Execution Error", e.getMessage());
                    verdictLabel.setText("ERROR");
                    verdictLabel.setStyle("-fx-text-fill: #ef4444;");
                    submitButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleClear() {
        problemNameField.clear();
        codeArea.clear();
        inputArea.clear();
        expectedOutputArea.clear();
        clearResult();
    }

    private void clearResult() {
        verdictLabel.setText("Waiting...");
        verdictLabel.setStyle("-fx-text-fill: #6b7280;");
        timeLabel.setText("- ms");
        actualOutputArea.clear();
        errorMessageArea.clear();
    }

    private void displayResult(SubmissionResult result) {
        // Set verdict
        String verdictText = result.getVerdict().getLabel();
        verdictLabel.setText(verdictText);

        // Set color based on verdict
        switch (result.getVerdict()) {
            case AC:
                verdictLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                break;
            case WA:
                verdictLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                break;
            case TLE:
                verdictLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                break;
            case RE:
                verdictLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                break;
            case CE:
                verdictLabel.setStyle("-fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
                break;
        }

        // Set execution time
        timeLabel.setText(result.getExecutionTime() + " ms");

        // Set actual output
        if (result.getActualOutput() != null && !result.getActualOutput().isEmpty()) {
            actualOutputArea.setText(result.getActualOutput());
        } else {
            actualOutputArea.setText("(No output)");
        }

        // Set error message if any
        if (result.getErrorMessage() != null && !result.getErrorMessage().isEmpty()) {
            errorMessageArea.setText(result.getErrorMessage());
        } else {
            errorMessageArea.clear();
        }

        // Log submission
        logSubmission(result);
    }

    private void logSubmission(SubmissionResult result) {
        String problemName = problemNameField.getText().trim();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] Problem: %s | Verdict: %s | Time: %dms",
                timestamp, problemName, result.getVerdict().getLabel(), result.getExecutionTime());
        System.out.println(logMessage);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

