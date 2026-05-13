package com.yourteam.ojaitester.controller;

import com.yourteam.ojaitester.dto.GeneratedCodeDto;
import com.yourteam.ojaitester.model.ExecutionResult;
import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.model.Submission;
import com.yourteam.ojaitester.model.SubmissionRunReport;
import com.yourteam.ojaitester.repository.SubmissionRepository;
import com.yourteam.ojaitester.repository.impl.SubmissionRepositoryImpl;
import com.yourteam.ojaitester.service.ProblemService;
import com.yourteam.ojaitester.service.SampleCodeGenerationService;
import com.yourteam.ojaitester.service.SubmissionService;
import com.yourteam.ojaitester.service.impl.GeminiSampleCodeGenerationService;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
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
	@FXML private TextArea generatedExplanationArea;

	@FXML private TableView<Submission> submissionTable;
	@FXML private TableColumn<Submission, Long> colSubmissionId;
	@FXML private TableColumn<Submission, String> colSubmissionName;
	@FXML private TableColumn<Submission, String> colSubmissionType;
	@FXML private TableColumn<Submission, String> colLanguage;
	@FXML private TableColumn<Submission, String> colCreatedAt;

	@FXML private TableView<ExecutionResult> resultTable;
	@FXML private TableColumn<ExecutionResult, Long> colResultTestcaseId;
	@FXML private TableColumn<ExecutionResult, String> colResultCategory;
	@FXML private TableColumn<ExecutionResult, String> colResultStatus;
	@FXML private TableColumn<ExecutionResult, String> colResultRuntimeMs;
	@FXML private TableColumn<ExecutionResult, String> colResultMemoryKb;
	@FXML private TableColumn<ExecutionResult, String> colResultExpectedOutput;
	@FXML private TableColumn<ExecutionResult, String> colResultActualOutput;
	@FXML private TableColumn<ExecutionResult, String> colResultErrorMessage;

	@FXML private TextArea runSummaryArea;
	@FXML private TextArea resultInputArea;
	@FXML private TextArea resultExpectedArea;
	@FXML private TextArea resultActualArea;
	@FXML private TextArea resultErrorArea;
	@FXML private Label statusLabel;
	@FXML private Button btnSave;
	@FXML private Button btnRun;
	@FXML private Button btnGenerateCode;
	@FXML private Button btnSaveGeneratedCode;
	@FXML private Button btnClear;
	@FXML private Button btnRefresh;

	private final ProblemService problemService = new ProblemServiceImpl();
	private final SubmissionService submissionService = new SubmissionServiceImpl();
	private final SubmissionRepository submissionRepository = new SubmissionRepositoryImpl();
	private final SampleCodeGenerationService sampleCodeGenerationService = new GeminiSampleCodeGenerationService();
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
		colResultCategory.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("category"));
		colResultStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
		colResultStatus.setCellFactory(column -> new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item.isBlank()) {
					setText(null);
					setStyle(null);
					return;
				}

				String status = item.trim().toUpperCase();
				setText(status);
				String color = switch (status) {
					case "AC" -> "#16a34a";
					case "WA" -> "#dc2626";
					case "TLE" -> "#d97706";
					case "CE" -> "#7c3aed";
					case "RE" -> "#6b7280";
					default -> "#111827";
				};
				setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
			}
		});
		colResultRuntimeMs.setCellValueFactory(data -> new SimpleStringProperty(formatInteger(data.getValue().getExecutionTimeMs()) + " ms"));
		colResultMemoryKb.setCellValueFactory(data -> new SimpleStringProperty(formatInteger(data.getValue().getMemoryKb()) + " KB"));
		colResultExpectedOutput.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("expectedOutput"));
		colResultActualOutput.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("actualOutput"));
		colResultErrorMessage.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("errorMessage"));

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
			draft = buildSubmissionFromFormForSave();
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
			draft = buildSubmissionFromFormAsNew();
		} catch (IllegalArgumentException ex) {
			showError("Validation Error", ex.getMessage());
			return;
		}
		Task<SubmissionRunReport> task = new Task<>() {
			@Override
			protected SubmissionRunReport call() {
				return submissionService.runSubmissionOnTestCases(draft, this::updateMessage);
			}
		};

		task.setOnRunning(e -> setBusy(true));
		task.messageProperty().addListener((obs, oldMessage, newMessage) -> {
			if (newMessage != null && !newMessage.isBlank()) {
				statusLabel.setText(newMessage);
			}
		});
		task.setOnSucceeded(e -> {
			setBusy(false);
			SubmissionRunReport report = task.getValue();
			runResults.setAll(report.getResults());
			showRunSummary(report);
			statusLabel.setText("Run completed: " + report.getOverallStatus());
			if ("NO_TESTCASE".equals(report.getOverallStatus())) {
				showError("Run Failed", "No testcases found for this problem.");
			} else if ("CE".equals(report.getOverallStatus())) {
				showError("Compilation Error", report.getSummaryMessage());
			} else if (report.getSummaryMessage() != null && !report.getSummaryMessage().isBlank()) {
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
	private void handleGenerateCodeByAI() {
		Problem selectedProblem = problemComboBox.getValue();
		String selectedType = submissionTypeComboBox.getValue();
		if (selectedProblem == null) {
			showError("Generate Code Failed", "Please select a problem first.");
			return;
		}
		if (selectedType == null || selectedType.isBlank()) {
			showError("Generate Code Failed", "Please select code type.");
			return;
		}
		if (selectedProblem.getRawText() == null || selectedProblem.getRawText().isBlank()) {
			showError("Generate Code Failed", "Problem statement is empty.");
			return;
		}

		Task<GeneratedCodeDto> task = new Task<>() {
			@Override
			protected GeneratedCodeDto call() {
				return sampleCodeGenerationService.generateCode(selectedProblem, selectedType);
			}
		};

		task.setOnRunning(e -> {
			setBusy(true);
			statusLabel.setText("Generating code...");
		});
		task.setOnSucceeded(e -> {
			setBusy(false);
			GeneratedCodeDto generatedCode = task.getValue();
			submissionTypeComboBox.setValue(selectedType);
			languageField.setText("CPP");
			submissionNameField.setText("AI Generated " + selectedType);
			sourceCodeArea.setText(generatedCode.getCode());
			generatedExplanationArea.setText(nullToEmpty(generatedCode.getExplanation()));
			statusLabel.setText("Generated " + selectedType + " code.");
		});
		task.setOnFailed(e -> {
			setBusy(false);
			statusLabel.setText("Code generation failed.");
			showError("Generate Code Failed", getRootMessage(task.getException()));
		});

		runTask(task, "sample-code-generation-task");
	}

	@FXML
	private void handleSaveGeneratedCode() {
		Problem selectedProblem = problemComboBox.getValue();
		String selectedType = submissionTypeComboBox.getValue();
		if (selectedProblem == null) {
			showError("Save Code Failed", "Please select a problem first.");
			return;
		}
		if (selectedType == null || selectedType.isBlank()) {
			showError("Save Code Failed", "Please select code type.");
			return;
		}
		if (!isValidGeneratedCode(sourceCodeArea.getText())) {
			showError("Save Code Failed", "Generated code is invalid.");
			return;
		}

		List<Submission> existing;
		try {
			existing = submissionRepository.findByProblemIdAndType(selectedProblem.getId(), selectedType);
		} catch (Exception ex) {
			showError("Save Code Failed", "Cannot save generated code.");
			return;
		}
		if (!existing.isEmpty() && !confirmReplaceGeneratedCode()) {
			statusLabel.setText("Save cancelled.");
			return;
		}

		Submission generatedSubmission = new Submission();
		generatedSubmission.setProblemId(selectedProblem.getId());
		generatedSubmission.setName("AI Generated " + selectedType);
		generatedSubmission.setSubmissionType(selectedType);
		generatedSubmission.setLanguage("CPP");
		generatedSubmission.setSourceCode(sourceCodeArea.getText());
		generatedSubmission.setNote(generatedExplanationArea.getText());

		Task<Submission> task = new Task<>() {
			@Override
			protected Submission call() {
				if (!existing.isEmpty()) {
					submissionRepository.deleteByProblemIdAndType(selectedProblem.getId(), selectedType);
				}
				return submissionService.saveSubmission(generatedSubmission);
			}
		};

		task.setOnRunning(e -> setBusy(true));
		task.setOnSucceeded(e -> {
			setBusy(false);
			statusLabel.setText("Saved generated " + selectedType + " code.");
			showInfo("Save Successful", "Generated code saved successfully.");
			refreshSubmissionTable();
		});
		task.setOnFailed(e -> {
			setBusy(false);
			showError("Save Code Failed", "Cannot save generated code.");
		});

		runTask(task, "generated-code-save-task");
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

	private Submission buildSubmissionFromFormForSave() {
		Submission submission = buildSubmissionBaseFromForm();
		Submission selectedRow = submissionTable.getSelectionModel().getSelectedItem();
		if (selectedRow != null) {
			submission.setId(selectedRow.getId());
		}
		return submission;
	}

	private Submission buildSubmissionFromFormAsNew() {
		return buildSubmissionBaseFromForm();
	}

	private Submission buildSubmissionBaseFromForm() {
		Problem selectedProblem = problemComboBox.getValue();
		if (selectedProblem == null) {
			throw new IllegalArgumentException("Please select a problem.");
		}

		Submission submission = new Submission();
		submission.setProblemId(selectedProblem.getId());
		submission.setName(submissionNameField.getText());
		submission.setSubmissionType(submissionTypeComboBox.getValue());
		submission.setLanguage(languageField.getText());
		submission.setSourceCode(sourceCodeArea.getText());
		submission.setNote(generatedExplanationArea.getText());
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
		generatedExplanationArea.setText(nullToEmpty(submission.getNote()));
		statusLabel.setText("Editing submission #" + submission.getId());
	}

	private void clearForm() {
		submissionNameField.clear();
		submissionTypeComboBox.setValue("AC");
		languageField.setText("C++");
		sourceCodeArea.clear();
		generatedExplanationArea.clear();
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
		text.append("AC: ").append(report.getPassedCount()).append('\n');
		text.append("WA: ").append(report.getWrongAnswerCount()).append('\n');
		text.append("TLE: ").append(report.getTleCount()).append('\n');
		text.append("RE: ").append(report.getRuntimeErrorCount()).append('\n');
		text.append("CE: ").append(report.getCompileErrorCount()).append('\n');
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
		btnGenerateCode.setDisable(busy);
		btnSaveGeneratedCode.setDisable(busy);
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

	private boolean confirmReplaceGeneratedCode() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Replace Generated Code");
		alert.setHeaderText(null);
		alert.setContentText("A generated code with this type already exists. Do you want to replace it?");
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
		Optional<ButtonType> result = alert.showAndWait();
		return result.isPresent() && result.get() == ButtonType.YES;
	}

	private boolean isValidGeneratedCode(String code) {
		return code != null && !code.isBlank() && code.contains("main");
	}

	private String getRootMessage(Throwable throwable) {
		if (throwable == null) {
			return "Unknown error";
		}
		Throwable current = throwable;
		while (current != null) {
			String message = current.getMessage();
			if (message != null) {
				if (message.contains("Cannot save execution results to database")) {
					return message;
				}
				if (message.contains("g++ compiler not found")) {
					return "g++ compiler not found. Please install MinGW or configure PATH.";
				}
				if (message.contains("Cannot create temporary source file")) {
					return "Cannot create temporary source file.";
				}
				if (message.contains("Cannot execute compiled program")) {
					return "Cannot execute compiled program.";
				}
			}
			current = current.getCause();
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


