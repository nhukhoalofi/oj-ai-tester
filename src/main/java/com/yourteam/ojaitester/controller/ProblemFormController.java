package com.yourteam.ojaitester.controller;
import com.yourteam.ojaitester.service.ProblemExtractionService;
import com.yourteam.ojaitester.service.impl.ProblemExtractionServiceImpl;
import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.service.ProblemService;
import com.yourteam.ojaitester.service.impl.ProblemServiceImpl;
import com.yourteam.ojaitester.util.FileUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ProblemFormController {

    @FXML
    private TextField titleField;

    @FXML
    private ComboBox<String> sourceTypeComboBox;

    @FXML
    private TextArea rawTextArea;

    @FXML
    private Label selectedFileLabel;

    private final ProblemService problemService = new ProblemServiceImpl();
    private final ProblemExtractionService extractionService = new ProblemExtractionServiceImpl();
    private File selectedFile;
    private boolean extractionSucceeded;

    @FXML
    public void initialize() {
        sourceTypeComboBox.setItems(FXCollections.observableArrayList(
                "TEXT",
                "IMAGE",
                "PDF"
        ));
        sourceTypeComboBox.setValue("TEXT");
        sourceTypeComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if ("TEXT".equals(newValue)) {
                clearSelectedFileState();
            }
        });
    }

    @FXML
    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Problem File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Supported Files", "*.pdf", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.webp"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.webp")
        );

        Stage stage = (Stage) titleField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedFile = file;
            selectedFileLabel.setText(file.getName());
            extractionSucceeded = false;
            rawTextArea.clear();

            if (FileUtils.isPdf(file)) {
                sourceTypeComboBox.setValue("PDF");
            } else if (FileUtils.isImage(file)) {
                sourceTypeComboBox.setValue("IMAGE");
            }
        }
    }

    @FXML
    private void handleLoadFromFile() {
        if (selectedFile == null) {
            showError("No File", "Please choose a file first.");
            return;
        }

        try {
            String text;

            if (FileUtils.isImage(selectedFile)) {
                text = extractionService.extractTextFromImage(selectedFile);
                sourceTypeComboBox.setValue("IMAGE");
            } else if (FileUtils.isPdf(selectedFile)) {
                text = extractionService.extractTextFromPdf(selectedFile);
                sourceTypeComboBox.setValue("PDF");
            } else {
                showError("Unsupported File", "Only PDF and image files are supported.");
                return;
            }

            rawTextArea.setText(text);
            extractionSucceeded = text != null && !text.isBlank();
            showInfo("Success", "Text extracted successfully.");

        } catch (Exception e) {
            extractionSucceeded = false;
            showError("Extraction Error", "Failed to extract text:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleSaveProblem() {
        String title = titleField.getText() != null ? titleField.getText().trim() : "";
        String sourceType = sourceTypeComboBox.getValue() != null ? sourceTypeComboBox.getValue() : "TEXT";
        String rawText = rawTextArea.getText() != null ? rawTextArea.getText().trim() : "";

        if (title.isEmpty()) {
            showError("Validation Error", "Title must not be empty.");
            return;
        }

        boolean isSourceFileBased = "IMAGE".equals(sourceType) || "PDF".equals(sourceType);

        if ("TEXT".equals(sourceType)) {
            if (rawText.isEmpty()) {
                showError("Validation Error", "Problem statement must not be empty.");
                return;
            }
        } else {
            if (selectedFile == null) {
                showError("Validation Error", "Please choose an image or PDF file first.");
                return;
            }

            if (!extractionSucceeded || rawText.isEmpty()) {
                showError("Validation Error",
                        "Please click Load Text From File to extract the problem statement before saving.");
                return;
            }
        }

        try {
            String savedPath = null;
            if (isSourceFileBased && selectedFile != null) {
                savedPath = FileUtils.saveUploadedFile(selectedFile, "problems");
            }

            Problem problem = new Problem();
            problem.setTitle(title);
            problem.setRawText(rawText);
            problem.setSourceType(sourceType);
            problem.setSourcePath(savedPath);
            problem.setStatus("NEW");

            Problem savedProblem = problemService.createProblem(problem);

            showInfo("Save Successful",
                    "Problem saved successfully.\nGenerated ID: " + savedProblem.getId());

            clearForm();

        } catch (Exception e) {
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
        clearSelectedFileState();
    }

    private void clearSelectedFileState() {
        selectedFile = null;
        extractionSucceeded = false;
        selectedFileLabel.setText("No file selected");
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
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}