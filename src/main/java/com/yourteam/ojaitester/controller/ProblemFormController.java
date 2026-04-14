package com.yourteam.ojaitester.controller;

import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.service.ProblemService;
import com.yourteam.ojaitester.service.impl.ProblemServiceImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ProblemFormController {

    @FXML
    private TextField titleField;

    @FXML
    private ComboBox<String> sourceTypeComboBox;

    @FXML
    private TextArea rawTextArea;

    private final ProblemService problemService = new ProblemServiceImpl();

    @FXML
    public void initialize() {
        sourceTypeComboBox.setItems(FXCollections.observableArrayList(
                "TEXT",
                "IMAGE",
                "FILE"
        ));
        sourceTypeComboBox.setValue("TEXT");
    }

    @FXML
    private void handleSaveProblem() {
        String title = titleField.getText() != null ? titleField.getText().trim() : "";
        String sourceType = sourceTypeComboBox.getValue();
        String rawText = rawTextArea.getText() != null ? rawTextArea.getText().trim() : "";

        if (title.isEmpty()) {
            showError("Validation Error", "Title must not be empty.");
            return;
        }

        if (rawText.isEmpty()) {
            showError("Validation Error", "Problem statement must not be empty.");
            return;
        }

        try {
            Problem problem = new Problem();
            problem.setTitle(title);
            problem.setRawText(rawText);
            problem.setSourceType(sourceType);
            problem.setSourcePath(null);
            problem.setStatus("NEW");

            Problem savedProblem = problemService.createProblem(problem);

            showInfo(
                    "Success",
                    "Problem saved successfully.\nGenerated ID: " + savedProblem.getId()
            );

            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Save Error", "Failed to save problem:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private void clearForm() {
        titleField.clear();
        rawTextArea.clear();
        sourceTypeComboBox.setValue("TEXT");
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}