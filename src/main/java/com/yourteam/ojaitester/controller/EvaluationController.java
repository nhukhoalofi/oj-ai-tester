package com.yourteam.ojaitester.controller;

import com.yourteam.ojaitester.model.EvaluationRow;
import com.yourteam.ojaitester.model.EvaluationSummary;
import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.model.Submission;
import com.yourteam.ojaitester.service.EvaluationService;
import com.yourteam.ojaitester.service.ProblemService;
import com.yourteam.ojaitester.repository.SubmissionRepository;
import com.yourteam.ojaitester.repository.impl.SubmissionRepositoryImpl;
import com.yourteam.ojaitester.service.impl.EvaluationServiceImpl;
import com.yourteam.ojaitester.service.impl.ProblemServiceImpl;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class EvaluationController {

    @FXML private ComboBox<Problem> problemComboBox;
    @FXML private Label statusLabel;
    @FXML private Button btnRefresh;
    @FXML private Button btnRunEvaluation;

    @FXML private TableView<Submission> submissionTable;
    @FXML private TableColumn<Submission, String> colSubmissionName;
    @FXML private TableColumn<Submission, String> colSubmissionType;
    @FXML private TableColumn<Submission, String> colSubmissionLanguage;

    @FXML private TableView<EvaluationRow> resultTable;
    @FXML private TableColumn<EvaluationRow, String> colTestcaseName;
    @FXML private TableColumn<EvaluationRow, String> colCategory;
    @FXML private TableColumn<EvaluationRow, String> colAcResult;
    @FXML private TableColumn<EvaluationRow, String> colWrongResults;
    @FXML private TableColumn<EvaluationRow, String> colComment;

    @FXML private Label totalTestcasesLabel;
    @FXML private Label acPassedLabel;
    @FXML private Label wrongDetectedLabel;
    @FXML private Label tleDetectedLabel;
    @FXML private Label strengthScoreLabel;

    private final ProblemService problemService = new ProblemServiceImpl();
    private final SubmissionRepository submissionRepository = new SubmissionRepositoryImpl();
    private final EvaluationService evaluationService = new EvaluationServiceImpl();
    private final ObservableList<Problem> problems = FXCollections.observableArrayList();
    private final ObservableList<Submission> submissions = FXCollections.observableArrayList();
    private final ObservableList<EvaluationRow> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        problemComboBox.setItems(problems);
        setupTables();
        setupProblemSelection();
        clearStats();
        setBusy(false);
        loadProblemsAsync();
    }

    private void setupTables() {
        colSubmissionName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        colSubmissionType.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("submissionType"));
        colSubmissionLanguage.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("language"));
        submissionTable.setItems(submissions);
        submissionTable.setPlaceholder(new Label("Select a problem to load submissions."));

        colTestcaseName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("testCaseName"));
        colCategory.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("category"));
        colAcResult.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("acResult"));
        colWrongResults.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("wrongCodeResults"));
        colComment.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("comment"));
        resultTable.setItems(rows);
        resultTable.setPlaceholder(new Label("Run evaluation to see testcase strength."));
    }

    private void setupProblemSelection() {
        problemComboBox.valueProperty().addListener((obs, oldProblem, newProblem) -> {
            rows.clear();
            clearStats();
            if (newProblem == null) {
                submissions.clear();
                statusLabel.setText("Please select a problem first.");
                return;
            }
            loadSubmissionsAsync(newProblem);
        });
    }

    @FXML
    private void handleRefresh() {
        loadProblemsAsync();
    }

    @FXML
    private void handleRunEvaluation() {
        Problem selected = problemComboBox.getValue();
        if (selected == null) {
            showError("Evaluation", "Please select a problem first.");
            return;
        }

        Task<EvaluationSummary> task = new Task<>() {
            @Override
            protected EvaluationSummary call() {
                return evaluationService.evaluateProblem(selected.getId(), this::updateMessage);
            }
        };

        task.setOnRunning(e -> {
            setBusy(true);
            statusLabel.setText("Running evaluation...");
        });
        task.messageProperty().addListener((obs, oldMessage, newMessage) -> {
            if (newMessage != null && !newMessage.isBlank()) {
                statusLabel.setText(newMessage);
            }
        });
        task.setOnSucceeded(e -> {
            setBusy(false);
            EvaluationSummary summary = task.getValue();
            rows.setAll(summary.getRows());
            showStats(summary);
            statusLabel.setText("Finished");
        });
        task.setOnFailed(e -> {
            setBusy(false);
            statusLabel.setText("Evaluation failed.");
            showError("Evaluation Failed", normalizeErrorMessage(task.getException()));
        });

        runTask(task, "evaluation-task");
    }

    private void loadProblemsAsync() {
        Task<List<Problem>> task = new Task<>() {
            @Override
            protected List<Problem> call() {
                return problemService.getAllProblems();
            }
        };

        task.setOnRunning(e -> {
            setBusy(true);
            statusLabel.setText("Loading problems...");
        });
        task.setOnSucceeded(e -> {
            setBusy(false);
            problems.setAll(task.getValue());
            statusLabel.setText("Ready");
        });
        task.setOnFailed(e -> {
            setBusy(false);
            showError("Load Problems Failed", normalizeErrorMessage(task.getException()));
        });

        runTask(task, "evaluation-problem-load-task");
    }

    private void loadSubmissionsAsync(Problem problem) {
        Task<List<Submission>> task = new Task<>() {
            @Override
            protected List<Submission> call() {
                return submissionRepository.findByProblemId(problem.getId());
            }
        };

        task.setOnRunning(e -> statusLabel.setText("Loading submissions..."));
        task.setOnSucceeded(e -> {
            submissions.setAll(task.getValue());
            statusLabel.setText("Loaded " + submissions.size() + " submissions.");
        });
        task.setOnFailed(e -> showError("Load Submissions Failed", normalizeErrorMessage(task.getException())));

        runTask(task, "evaluation-submission-load-task");
    }

    private void showStats(EvaluationSummary summary) {
        totalTestcasesLabel.setText(String.valueOf(summary.getTotalTestcases()));
        acPassedLabel.setText(summary.getAcPassedTestcases() + " / " + summary.getTotalTestcases());
        wrongDetectedLabel.setText(summary.getDetectedWrongSubmissions() + " / " + summary.getTotalWrongSubmissions());
        tleDetectedLabel.setText(String.valueOf(summary.getDetectedTleSubmissions()));
        strengthScoreLabel.setText(String.format("%.2f%%", summary.getStrengthScore()));
    }

    private void clearStats() {
        totalTestcasesLabel.setText("-");
        acPassedLabel.setText("-");
        wrongDetectedLabel.setText("-");
        tleDetectedLabel.setText("-");
        strengthScoreLabel.setText("-");
    }

    private void setBusy(boolean busy) {
        btnRefresh.setDisable(busy);
        btnRunEvaluation.setDisable(busy);
        problemComboBox.setDisable(busy);
    }

    private void runTask(Task<?> task, String name) {
        Thread worker = new Thread(task, name);
        worker.setDaemon(true);
        worker.start();
    }

    private String normalizeErrorMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && !message.isBlank()) {
                if (message.contains("No testcases found")) return "No testcases found for this problem.";
                if (message.contains("At least one AC")) return "At least one AC solution is required for evaluation.";
                if (message.contains("At least one WA or TLE")) return "At least one WA or TLE solution is required to evaluate testcase strength.";
                if (message.contains("g++ compiler not found")) return "g++ compiler not found. Please install MinGW or configure PATH.";
                if (message.contains("Cannot save execution results")) return "Cannot save evaluation results to database.";
                return message;
            }
            current = current.getCause();
        }
        return "Unexpected error.";
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
