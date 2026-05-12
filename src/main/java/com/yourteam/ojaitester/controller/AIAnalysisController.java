package com.yourteam.ojaitester.controller;

import com.yourteam.ojaitester.model.ParsedProblem;
import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.service.ProblemAnalysisService;
import com.yourteam.ojaitester.service.ProblemService;
import com.yourteam.ojaitester.service.impl.GeminiProblemAnalysisService;
import com.yourteam.ojaitester.service.impl.ProblemServiceImpl;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AIAnalysisController {

    @FXML private TableView<Problem> problemTable;
    @FXML private TableColumn<Problem, Long> colId;
    @FXML private TableColumn<Problem, String> colTitle;
    @FXML private TableColumn<Problem, String> colSourceType;
    @FXML private TableColumn<Problem, String> colStatus;
    @FXML private TableColumn<Problem, String> colCreatedAt;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Button btnRefresh;
    @FXML private Button btnAnalyze;
    @FXML private TextArea rawTextArea;
    @FXML private TextField parsedTitleField;
    @FXML private TextArea parsedStatementArea;
    @FXML private TextArea parsedInputFormatArea;
    @FXML private TextArea parsedOutputFormatArea;
    @FXML private TextArea parsedConstraintsArea;
    @FXML private TextField parsedTagsField;
    @FXML private TextArea parsedSummaryArea;

    private final ProblemService problemService = new ProblemServiceImpl();
    private final ProblemAnalysisService problemAnalysisService = new GeminiProblemAnalysisService();
    private final ObservableList<Problem> problems = FXCollections.observableArrayList();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupTable();
        setupSelectionListener();
        setupSearch();
        setBusy(false);
        loadProblemsAsync();
        clearProblemDetails();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colSourceType.setCellValueFactory(new PropertyValueFactory<>("sourceType"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                    return;
                }
                setText(item);
                String color = switch (item) {
                    case "ANALYZED" -> "#16a34a";
                    case "NEW" -> "#2563eb";
                    default -> "#111827";
                };
                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            }
        });
        colCreatedAt.setCellValueFactory(data -> {
            Problem problem = data.getValue();
            String text = problem.getCreatedAt() != null ? problem.getCreatedAt().format(FORMATTER) : "-";
            return new SimpleStringProperty(text);
        });
        problemTable.setItems(problems);
        problemTable.setPlaceholder(new Label("No problems available."));
    }

    private void setupSelectionListener() {
        problemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                clearProblemDetails();
                statusLabel.setText("Please select a problem.");
                return;
            }
            rawTextArea.setText(newValue.getRawText() != null ? newValue.getRawText() : "");
            clearParsedProblem();
            statusLabel.setText("Selected: " + safeText(newValue.getTitle()));
            loadParsedProblemAsync(newValue);
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterProblems(newValue));
    }

    @FXML
    private void handleRefresh() {
        loadProblemsAsync();
    }

    @FXML
    private void handleAnalyze() {
        Problem selected = problemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Analysis Error", "Please select a problem first.");
            return;
        }
        if (selected.getRawText() == null || selected.getRawText().isBlank()) {
            showError("Analysis Error", "Problem statement is empty.");
            return;
        }

        Task<ParsedProblem> task = new Task<>() {
            @Override
            protected ParsedProblem call() {
                return problemAnalysisService.analyzeAndSave(selected);
            }
        };

        task.setOnRunning(e -> {
            setBusy(true);
            statusLabel.setText("Analyzing...");
        });
        task.setOnSucceeded(e -> {
            ParsedProblem parsedProblem = task.getValue();
            displayParsedProblem(parsedProblem);
            selected.setStatus("ANALYZED");
            problemTable.refresh();
            statusLabel.setText("AI analysis completed.");
            setBusy(false);
            showInfo("Analyze by AI", "Problem analysis saved successfully.");
        });
        task.setOnFailed(e -> {
            setBusy(false);
            statusLabel.setText("AI analysis failed.");
            showError("Analyze by AI Failed", normalizeAnalysisError(task.getException()));
        });

        Thread worker = new Thread(task, "ai-analysis-task");
        worker.setDaemon(true);
        worker.start();
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
            problems.setAll(task.getValue());
            filterProblems(searchField.getText());
            setBusy(false);
            statusLabel.setText("Loaded " + problems.size() + " problems.");
        });
        task.setOnFailed(e -> {
            setBusy(false);
            showError("Load Problems Failed", getRootMessage(task.getException()));
        });

        Thread worker = new Thread(task, "ai-analysis-problem-loader");
        worker.setDaemon(true);
        worker.start();
    }

    private void filterProblems(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            problemTable.setItems(problems);
            return;
        }
        String lower = keyword.toLowerCase();
        ObservableList<Problem> filtered = FXCollections.observableArrayList(
                problems.stream()
                        .filter(problem -> contains(problem.getTitle(), lower)
                                || contains(problem.getSourceType(), lower)
                                || contains(problem.getStatus(), lower))
                        .toList()
        );
        problemTable.setItems(filtered);
    }

    private void loadParsedProblemAsync(Problem problem) {
        if (problem == null || problem.getId() == null) {
            return;
        }
        Task<ParsedProblem> task = new Task<>() {
            @Override
            protected ParsedProblem call() {
                return problemAnalysisService.getParsedProblem(problem.getId()).orElse(null);
            }
        };
        task.setOnSucceeded(e -> displayParsedProblem(task.getValue()));

        Thread worker = new Thread(task, "ai-analysis-parsed-loader");
        worker.setDaemon(true);
        worker.start();
    }

    private void displayParsedProblem(ParsedProblem parsedProblem) {
        if (parsedProblem == null) {
            clearParsedProblem();
            return;
        }
        parsedTitleField.setText(safeText(parsedProblem.getTitle()));
        parsedStatementArea.setText(safeText(parsedProblem.getStatement()));
        parsedInputFormatArea.setText(safeText(parsedProblem.getInputFormat()));
        parsedOutputFormatArea.setText(safeText(parsedProblem.getOutputFormat()));
        parsedConstraintsArea.setText(safeText(parsedProblem.getConstraintsText()));
        parsedTagsField.setText(safeText(parsedProblem.getTags()));
        parsedSummaryArea.setText(safeText(parsedProblem.getSummary()));
    }

    private void clearProblemDetails() {
        rawTextArea.clear();
        clearParsedProblem();
    }

    private void clearParsedProblem() {
        parsedTitleField.clear();
        parsedStatementArea.clear();
        parsedInputFormatArea.clear();
        parsedOutputFormatArea.clear();
        parsedConstraintsArea.clear();
        parsedTagsField.clear();
        parsedSummaryArea.clear();
    }

    private void setBusy(boolean busy) {
        btnAnalyze.setDisable(busy);
        btnRefresh.setDisable(busy);
        problemTable.setDisable(busy);
    }

    private boolean contains(String value, String lowerKeyword) {
        return value != null && value.toLowerCase().contains(lowerKeyword);
    }

    private String safeText(String value) {
        return value != null ? value : "";
    }

    private String normalizeAnalysisError(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor != null) {
            String message = cursor.getMessage();
            if (message != null) {
                if (message.contains("Gemini API key is missing")) {
                    return "Gemini API key is missing.";
                }
                if (message.contains("AI response is not valid JSON")) {
                    return "AI response is not valid JSON.";
                }
                if (message.contains("Cannot save parsed problem")) {
                    return message;
                }
                if (message.contains("Cannot prepare parsed_problems schema")) {
                    return message;
                }
            }
            cursor = cursor.getCause();
        }
        return getRootMessage(throwable);
    }

    private String getRootMessage(Throwable throwable) {
        if (throwable == null) {
            return "Unknown error";
        }
        Throwable root = throwable;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : root.toString();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
