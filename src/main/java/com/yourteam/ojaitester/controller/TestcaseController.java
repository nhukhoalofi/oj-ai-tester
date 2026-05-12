package com.yourteam.ojaitester.controller;

import com.yourteam.ojaitester.model.GeneratedTestCase;
import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.model.TestCase;
import com.yourteam.ojaitester.repository.TestCaseRepository;
import com.yourteam.ojaitester.repository.impl.TestCaseRepositoryImpl;
import com.yourteam.ojaitester.service.ProblemService;
import com.yourteam.ojaitester.service.TestcaseGenerationService;
import com.yourteam.ojaitester.service.impl.GeminiTestcaseGenerationService;
import com.yourteam.ojaitester.service.impl.ProblemServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class TestcaseController {

    @FXML private ComboBox<Problem> problemComboBox;
    @FXML private Label statusLabel;
    @FXML private Button btnRefreshProblems;
    @FXML private Button btnGenerateTestcases;
    @FXML private Button btnRefreshTestcases;
    @FXML private Button btnDeleteTestcase;
    @FXML private Button btnClearTestcases;

    @FXML private TableView<TestCase> testcaseTable;
    @FXML private TableColumn<TestCase, Long> colTestcaseId;
    @FXML private TableColumn<TestCase, String> colTestcaseCategory;
    @FXML private TableColumn<TestCase, String> colTestcaseInput;
    @FXML private TableColumn<TestCase, String> colTestcaseExpectedOutput;
    @FXML private TableColumn<TestCase, String> colTestcasePurpose;

    @FXML private TextArea inputArea;
    @FXML private TextArea expectedOutputArea;
    @FXML private TextArea purposeArea;

    private final ProblemService problemService = new ProblemServiceImpl();
    private final TestcaseGenerationService testcaseGenerationService = new GeminiTestcaseGenerationService();
    private final TestCaseRepository testCaseRepository = new TestCaseRepositoryImpl();
    private final ObservableList<Problem> problems = FXCollections.observableArrayList();
    private final ObservableList<TestCase> testcases = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        problemComboBox.setItems(problems);
        setupTable();
        setupSelectionListeners();
        setTestcaseActionsDisabled(true);
        setBusy(false);
        loadProblemsAsync();
    }

    private void setupTable() {
        colTestcaseId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTestcaseCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colTestcaseInput.setCellValueFactory(new PropertyValueFactory<>("inputData"));
        colTestcaseExpectedOutput.setCellValueFactory(new PropertyValueFactory<>("expectedOutput"));
        colTestcasePurpose.setCellValueFactory(new PropertyValueFactory<>("purpose"));

        testcaseTable.setItems(testcases);
        testcaseTable.setPlaceholder(new Label("Select a problem to view testcases."));
    }

    private void setupSelectionListeners() {
        problemComboBox.valueProperty().addListener((obs, oldProblem, newProblem) -> {
            testcaseTable.getSelectionModel().clearSelection();
            clearDetails();
            if (newProblem == null) {
                testcases.clear();
                statusLabel.setText("Please select a problem first.");
                setTestcaseActionsDisabled(true);
                setBusy(false);
                return;
            }
            setBusy(false);
            loadTestcasesAsync(newProblem);
        });

        testcaseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                clearDetails();
                return;
            }
            inputArea.setText(nullToEmpty(selected.getInputData()));
            expectedOutputArea.setText(nullToEmpty(selected.getExpectedOutput()));
            purposeArea.setText(nullToEmpty(selected.getPurpose()));
        });
    }

    @FXML
    private void handleRefreshProblems() {
        loadProblemsAsync();
    }

    @FXML
    private void handleRefreshTestcases() {
        Problem selected = problemComboBox.getValue();
        if (selected == null) {
            showError("Refresh Testcases", "Please select a problem first.");
            return;
        }
        loadTestcasesAsync(selected);
    }

    @FXML
    private void handleGenerateTestcases() {
        Problem selected = problemComboBox.getValue();
        if (selected == null) {
            showError("Generate Testcases", "Please select a problem first.");
            return;
        }
        if (selected.getRawText() == null || selected.getRawText().isBlank()) {
            showError("Generate Testcases", "Problem statement is empty.");
            return;
        }

        boolean replaceExisting = !testcases.isEmpty();
        if (replaceExisting && !confirmReplaceExistingTestcases()) {
            return;
        }

        setBusy(true);
        statusLabel.setText("Generating testcases...");

        Task<List<GeneratedTestCase>> task = new Task<>() {
            @Override
            protected List<GeneratedTestCase> call() {
                List<GeneratedTestCase> generated = testcaseGenerationService.generateTestcases(selected);
                if (generated == null || generated.isEmpty()) {
                    throw new IllegalArgumentException("No testcases generated.");
                }
                if (replaceExisting) {
                    testCaseRepository.deleteByProblemId(selected.getId());
                }
                return testCaseRepository.saveAll(selected.getId(), generated);
            }
        };

        task.setOnSucceeded(e -> {
            testcases.setAll(task.getValue());
            statusLabel.setText("Generated " + testcases.size() + " testcases.");
            setBusy(false);
            setTestcaseActionsDisabled(testcases.isEmpty());
        });

        task.setOnFailed(e -> {
            statusLabel.setText("Generate testcases failed.");
            setBusy(false);
            setTestcaseActionsDisabled(testcases.isEmpty());
            showError("Generate Testcases", normalizeErrorMessage(task.getException()));
        });

        Thread worker = new Thread(task, "testcase-generation-task");
        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    private void handleDeleteTestcase() {
        TestCase selected = testcaseTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) {
            showError("Delete Testcase", "Please select a testcase first.");
            return;
        }

        try {
            testCaseRepository.deleteById(selected.getId());
            testcases.remove(selected);
            clearDetails();
            statusLabel.setText("Deleted testcase. Remaining: " + testcases.size());
            setTestcaseActionsDisabled(testcases.isEmpty());
        } catch (Exception e) {
            showError("Delete Testcase", "Cannot delete testcase from database.");
        }
    }

    @FXML
    private void handleClearTestcases() {
        Problem selected = problemComboBox.getValue();
        if (selected == null) {
            showError("Clear Testcases", "Please select a problem first.");
            return;
        }
        if (testcases.isEmpty()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear Testcases");
        confirm.setHeaderText("Delete all testcases for this problem?");
        confirm.setContentText("Existing execution results for these testcases will also be deleted.");
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;
            try {
                testCaseRepository.deleteByProblemId(selected.getId());
                testcases.clear();
                clearDetails();
                statusLabel.setText("No testcases.");
                setTestcaseActionsDisabled(true);
            } catch (Exception e) {
                showError("Clear Testcases", "Cannot delete testcases from database.");
            }
        });
    }

    private void loadProblemsAsync() {
        setBusy(true);
        statusLabel.setText("Loading problems...");

        Task<List<Problem>> task = new Task<>() {
            @Override
            protected List<Problem> call() {
                return problemService.getAllProblems();
            }
        };

        task.setOnSucceeded(e -> {
            Problem previous = problemComboBox.getValue();
            problems.setAll(task.getValue());
            if (previous != null) {
                problems.stream()
                        .filter(problem -> previous.getId().equals(problem.getId()))
                        .findFirst()
                        .ifPresent(problemComboBox::setValue);
            }
            statusLabel.setText("Loaded " + problems.size() + " problems.");
            setBusy(false);
            setTestcaseActionsDisabled(testcases.isEmpty());
        });

        task.setOnFailed(e -> {
            statusLabel.setText("Cannot load problems.");
            setBusy(false);
            showError("Load Problems", normalizeErrorMessage(task.getException()));
        });

        Thread worker = new Thread(task, "testcase-problem-loader");
        worker.setDaemon(true);
        worker.start();
    }

    private void loadTestcasesAsync(Problem problem) {
        statusLabel.setText("Loading testcases...");
        setTestcaseActionsDisabled(true);

        Task<List<TestCase>> task = new Task<>() {
            @Override
            protected List<TestCase> call() {
                return testCaseRepository.findByProblemId(problem.getId());
            }
        };

        task.setOnSucceeded(e -> {
            testcases.setAll(task.getValue());
            statusLabel.setText(testcases.isEmpty()
                    ? "No testcases for selected problem."
                    : "Loaded " + testcases.size() + " testcases.");
            setTestcaseActionsDisabled(testcases.isEmpty());
        });

        task.setOnFailed(e -> {
            testcases.clear();
            statusLabel.setText("Cannot load testcases.");
            showError("Load Testcases", normalizeErrorMessage(task.getException()));
        });

        Thread worker = new Thread(task, "testcase-list-loader");
        worker.setDaemon(true);
        worker.start();
    }

    private void setBusy(boolean busy) {
        btnRefreshProblems.setDisable(busy);
        btnGenerateTestcases.setDisable(busy || problemComboBox.getValue() == null);
        btnRefreshTestcases.setDisable(busy || problemComboBox.getValue() == null);
    }

    private void setTestcaseActionsDisabled(boolean disabled) {
        btnDeleteTestcase.setDisable(disabled);
        btnClearTestcases.setDisable(disabled);
    }

    private boolean confirmReplaceExistingTestcases() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Generate Testcases");
        confirm.setHeaderText("Do you want to replace existing testcases?");
        confirm.setContentText("Choosing OK deletes current testcases for this problem before saving new ones.");
        return confirm.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    private void clearDetails() {
        inputArea.clear();
        expectedOutputArea.clear();
        purposeArea.clear();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String normalizeErrorMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && !message.isBlank()) {
                if (message.contains("Gemini API key is missing")) return "Gemini API key is missing.";
                if (message.contains("Gemini API timed out") || message.toLowerCase().contains("timeout")) {
                    return "Gemini API timed out. Please try again, or shorten the problem statement.";
                }
                if (message.contains("AI response is not valid JSON array")) return "AI response is not valid JSON array.";
                if (message.contains("Cannot save testcases to database")) return message;
                if (message.contains("No testcases generated")) return "No testcases generated.";
                return message;
            }
            current = current.getCause();
        }
        return "Unexpected error.";
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
