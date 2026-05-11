package com.yourteam.ojaitester.controller;

import com.yourteam.ojaitester.model.ExecutionResult;
import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.model.Submission;
import com.yourteam.ojaitester.model.SubmissionRunReport;
import com.yourteam.ojaitester.service.ProblemService;
import com.yourteam.ojaitester.service.SubmissionService;
import com.yourteam.ojaitester.service.impl.ProblemServiceImpl;
import com.yourteam.ojaitester.service.impl.SubmissionServiceImpl;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class SubmissionController {

	@FXML private ComboBox<Problem> problemComboBox;
	@FXML private TextField submissionNameField;
	@FXML private ComboBox<String> submissionTypeComboBox;
	@FXML private TextField languageField;
	@FXML private TextArea sourceCodeArea;

	@FXML private TableView<Submission> submissionTable;
	@FXML private TableColumn<Submission, Long> colSubmissionId;
	@FXML private TableColumn<Submission, String> colSubmissionName;
	@FXML private TableColumn<Submission, String> colSubmissionType;
	@FXML private TableColumn<Submission, String> colLanguage;
	@FXML private TableColumn<Submission, String> colCreatedAt;

	@FXML private TableView<ExecutionResult> resultTable;
	@FXML private TableColumn<ExecutionResult, Long> colResultTestcaseId;
	@FXML private TableColumn<ExecutionResult, String> colResultStatus;
	@FXML private TableColumn<ExecutionResult, String> colResultRuntimeMs;
	@FXML private TableColumn<ExecutionResult, String> colResultMemoryKb;

	@FXML private TextArea runSummaryArea;
	@FXML private TextArea resultInputArea;
	@FXML private TextArea resultExpectedArea;
	@FXML private TextArea resultActualArea;
	@FXML private TextArea resultErrorArea;
	@FXML private Label statusLabel;
	@FXML private Button btnSave;
	@FXML private Button btnRun;
	@FXML private Button btnClear;
	@FXML private Button btnRefresh;

	private final ProblemService problemService = new ProblemServiceImpl();
	private final SubmissionService submissionService = new SubmissionServiceImpl();
	private final ObservableList<Problem> problems = FXCollections.observableArrayList();
	private final ObservableList<Submission> submissions = FXCollections.observableArrayList();
	private final ObservableList<ExecutionResult> runResults = FXCollections.observableArrayList();

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	@FXML
	public void initialize() {
		submissionTypeComboBox.setItems(FXCollections.observableArrayList("AC", "WA", "TLE"));
		submissionTypeComboBox.setValue("AC");
		languageField.setText("C++");
		languageField.setEditable(false);

		setupProblemComboBox();
		setupSubmissionTable();
		setupResultTable();
		setupSubmissionSelectionListener();
		setupResultSelectionListener();
		setupProblemSelectionListener();
		setBusy(false);
		loadProblemsAsync();
		clearForm();
		clearResultDetails();
	}

	private void setupProblemComboBox() {
		problemComboBox.setItems(problems);
	}

	private void setupSubmissionTable() {
		colSubmissionId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
		colSubmissionName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
		colSubmissionType.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("submissionType"));
		colLanguage.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("language"));
		colCreatedAt.setCellValueFactory(data -> {
			Submission submission = data.getValue();
			String text = submission.getCreatedAt() != null ? submission.getCreatedAt().format(FORMATTER) : "-";
			return new SimpleStringProperty(text);
		});

		submissionTable.setItems(submissions);
		submissionTable.setPlaceholder(new Label("Chọn một problem để xem submissions."));
	}

	private void setupResultTable() {
		colResultTestcaseId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("testcaseId"));
		colResultStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
		colResultRuntimeMs.setCellValueFactory(data -> new SimpleStringProperty(formatInteger(data.getValue().getExecutionTimeMs()) + " ms"));
		colResultMemoryKb.setCellValueFactory(data -> new SimpleStringProperty(formatInteger(data.getValue().getMemoryKb()) + " KB"));

		resultTable.setItems(runResults);
		resultTable.setPlaceholder(new Label("Run a submission to see testcase results."));
	}

	private void setupSubmissionSelectionListener() {
		submissionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) {
				populateForm(newValue);
				statusLabel.setText("Loaded submission: " + newValue.getName());
			}
		});
	}

	private void setupResultSelectionListener() {
		resultTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) {
				resultInputArea.setText(nullToEmpty(newValue.getInputData()));
				resultExpectedArea.setText(nullToEmpty(newValue.getExpectedOutput()));
				resultActualArea.setText(nullToEmpty(newValue.getActualOutput()));
				resultErrorArea.setText(nullToEmpty(newValue.getErrorMessage()));
			} else {
				clearResultDetails();
			}
		});
	}

	private void setupProblemSelectionListener() {
		problemComboBox.valueProperty().addListener((obs, oldProblem, newProblem) -> {
			if (newProblem == null) {
				submissions.clear();
				submissionTable.getSelectionModel().clearSelection();
				resultTable.getSelectionModel().clearSelection();
				clearForm();
				runResults.clear();
				clearResultDetails();
				runSummaryArea.clear();
				statusLabel.setText("Please select a problem.");
				return;
			}
			submissionTable.getSelectionModel().clearSelection();
			resultTable.getSelectionModel().clearSelection();
			clearForm();
			runResults.clear();
			clearResultDetails();
			runSummaryArea.clear();
			refreshSubmissionTable();
		});
	}

	@FXML
	private void handleProblemSelected() {
		// handled by the value-property listener to avoid duplicate reloads
	}

	@FXML
	private void handleSaveSubmission() {
		Submission draft;
		try {
			draft = buildSubmissionFromForm();
		} catch (IllegalArgumentException ex) {
			showError("Validation Error", ex.getMessage());
			return;
		}
		Task<Submission> task = new Task<>() {
			@Override
			protected Submission call() {
				return submissionService.saveSubmission(draft);
			}
		};

		task.setOnRunning(e -> setBusy(true));
		task.setOnSucceeded(e -> {
			setBusy(false);
			Submission saved = task.getValue();
			statusLabel.setText("Saved submission #" + saved.getId());
			showInfo("Save Successful", "Submission saved successfully.");
			clearForm();
			submissionTable.getSelectionModel().clearSelection();
			refreshSubmissionTable();
		});
		task.setOnFailed(e -> {
			setBusy(false);
			showError("Save Failed", getRootMessage(task.getException()));
		});

		runTask(task, "submission-save-task");
	}

	@FXML
	private void handleRunSubmission() {
		Submission draft;
		try {
			draft = buildSubmissionFromForm();
		} catch (IllegalArgumentException ex) {
			showError("Validation Error", ex.getMessage());
			return;
		}
		Task<SubmissionRunReport> task = new Task<>() {
			@Override
			protected SubmissionRunReport call() {
				return submissionService.runSubmissionOnTestCases(draft);
			}
		};

		task.setOnRunning(e -> setBusy(true));
		task.setOnSucceeded(e -> {
			setBusy(false);
			SubmissionRunReport report = task.getValue();
			runResults.setAll(report.getResults());
			showRunSummary(report);
			statusLabel.setText("Run completed: " + report.getOverallStatus());
			if (report.getSummaryMessage() != null && !report.getSummaryMessage().isBlank()) {
				showInfo("Run Completed", report.getSummaryMessage());
			}
			refreshSubmissionTable();
		});
		task.setOnFailed(e -> {
			setBusy(false);
			showError("Run Failed", getRootMessage(task.getException()));
		});

		runTask(task, "submission-run-task");
	}

	@FXML
	private void handleClearForm() {
		clearForm();
		submissionTable.getSelectionModel().clearSelection();
		resultTable.getSelectionModel().clearSelection();
		runResults.clear();
		clearResultDetails();
		runSummaryArea.clear();
		statusLabel.setText("Form cleared.");
	}

	@FXML
	private void handleRefreshSubmissions() {
		refreshSubmissionTable();
	}

	private void loadProblemsAsync() {
		Task<List<Problem>> task = new Task<>() {
			@Override
			protected List<Problem> call() {
				return problemService.getAllProblems();
			}
		};

		task.setOnSucceeded(e -> {
			problems.setAll(task.getValue());
			if (problems.isEmpty()) {
				statusLabel.setText("No problems found in database.");
				submissionTable.setPlaceholder(new Label("No problems available."));
			} else {
				statusLabel.setText("Loaded " + problems.size() + " problems. Please select one.");
				submissionTable.setPlaceholder(new Label("Select a problem to load submissions."));
			}
		});
		task.setOnFailed(e -> showError("Load Problems Failed", getRootMessage(task.getException())));

		runTask(task, "problem-load-task");
	}

	private void refreshSubmissionTable() {
		Problem selectedProblem = problemComboBox.getValue();
		if (selectedProblem == null || selectedProblem.getId() == null) {
			submissions.clear();
			statusLabel.setText("Please select a problem.");
			return;
		}

		Task<List<Submission>> task = new Task<>() {
			@Override
			protected List<Submission> call() {
				return submissionService.getSubmissionsByProblemId(selectedProblem.getId());
			}
		};

		task.setOnRunning(e -> setBusy(true));
		task.setOnSucceeded(e -> {
			setBusy(false);
			submissions.setAll(task.getValue());
			statusLabel.setText("Loaded " + submissions.size() + " submissions for " + selectedProblem.getTitle());
		});
		task.setOnFailed(e -> {
			setBusy(false);
			showError("Load Submissions Failed", getRootMessage(task.getException()));
		});

		runTask(task, "submission-list-load-task");
	}

	private Submission buildSubmissionFromForm() {
		Problem selectedProblem = problemComboBox.getValue();
		if (selectedProblem == null) {
			throw new IllegalArgumentException("Please select a problem.");
		}

		Submission submission = new Submission();
		Submission selectedRow = submissionTable.getSelectionModel().getSelectedItem();
		if (selectedRow != null) {
			submission.setId(selectedRow.getId());
		}
		submission.setProblemId(selectedProblem.getId());
		submission.setName(submissionNameField.getText());
		submission.setSubmissionType(submissionTypeComboBox.getValue());
		submission.setLanguage(languageField.getText());
		submission.setSourceCode(sourceCodeArea.getText());
		return submission;
	}

	private void populateForm(Submission submission) {
		Optional<Problem> matchedProblem = problems.stream()
				.filter(problem -> problem.getId() != null && problem.getId().equals(submission.getProblemId()))
				.findFirst();
		matchedProblem.ifPresent(problemComboBox::setValue);
		submissionNameField.setText(submission.getName());
		submissionTypeComboBox.setValue(submission.getSubmissionType());
		languageField.setText(submission.getLanguage() != null ? submission.getLanguage() : "C++");
		sourceCodeArea.setText(submission.getSourceCode());
		statusLabel.setText("Editing submission #" + submission.getId());
	}

	private void clearForm() {
		submissionNameField.clear();
		submissionTypeComboBox.setValue("AC");
		languageField.setText("C++");
		sourceCodeArea.clear();
	}

	private void clearResultDetails() {
		resultInputArea.clear();
		resultExpectedArea.clear();
		resultActualArea.clear();
		resultErrorArea.clear();
	}

	private void showRunSummary(SubmissionRunReport report) {
		StringBuilder text = new StringBuilder();
		text.append("Submission: ")
				.append(report.getSubmissionName() != null ? report.getSubmissionName() : "-")
				.append(" (#")
				.append(report.getSubmissionId() != null ? report.getSubmissionId() : "-")
				.append(")\n");
		text.append("Overall Status: ").append(report.getOverallStatus()).append('\n');
		text.append("Total Testcases: ").append(report.getTotalTestcases()).append('\n');
		text.append("Passed: ").append(report.getPassedCount()).append(" / ").append(report.getTotalTestcases()).append('\n');
		text.append("Failed: ").append(report.getFailedCount()).append('\n');
		text.append("Total Runtime: ").append(report.getTotalRuntimeMs()).append(" ms\n");
		text.append("\n").append(report.getSummaryMessage() != null ? report.getSummaryMessage() : "");
		runSummaryArea.setText(text.toString().trim());
		if (!runResults.isEmpty()) {
			resultTable.getSelectionModel().selectFirst();
		}
	}

	private void setBusy(boolean busy) {
		btnSave.setDisable(busy);
		btnRun.setDisable(busy);
		btnClear.setDisable(busy);
		btnRefresh.setDisable(busy);
		problemComboBox.setDisable(busy);
		submissionTable.setDisable(busy);
		resultTable.setDisable(busy);
	}

	private void runTask(Task<?> task, String threadName) {
		Thread worker = new Thread(task, threadName);
		worker.setDaemon(true);
		worker.start();
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

	private String formatInteger(Integer value) {
		return value != null ? value.toString() : "-";
	}

	private String nullToEmpty(String value) {
		return value != null ? value : "";
	}
}


