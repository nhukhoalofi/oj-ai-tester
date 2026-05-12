package com.yourteam.ojaitester.controller;

import com.yourteam.ojaitester.model.Problem;
import com.yourteam.ojaitester.service.ProblemService;
import com.yourteam.ojaitester.service.impl.ProblemServiceImpl;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProblemListController {

    // ── Table ──────────────────────────────────────────────────────────────────
    @FXML private TableView<Problem>          problemTable;
    @FXML private TableColumn<Problem, Long>  colId;
    @FXML private TableColumn<Problem, String> colTitle;
    @FXML private TableColumn<Problem, String> colSourceType;
    @FXML private TableColumn<Problem, String> colStatus;
    @FXML private TableColumn<Problem, String> colCreatedAt;

    // ── Toolbar ────────────────────────────────────────────────────────────────
    @FXML private TextField searchField;
    @FXML private Label     statusLabel;
    @FXML private Label     loadingLabel;
    @FXML private Button    btnRefresh;

    // ── Detail panel ──────────────────────────────────────────────────────────
    @FXML private Label   detailTitle;
    @FXML private Label   detailSourceType;
    @FXML private Label   detailStatus;
    @FXML private Label   detailCreatedAt;
    @FXML private Label   detailSourcePath;
    @FXML private TextArea detailRawText;

    // ── Action buttons ────────────────────────────────────────────────────────
    @FXML private Button btnOpenSourceFile;
    @FXML private Button btnDelete;

    // ── State ─────────────────────────────────────────────────────────────────
    private final ProblemService             problemService = new ProblemServiceImpl();
    private final ObservableList<Problem>    masterList     = FXCollections.observableArrayList();
    private       FilteredList<Problem>      filteredList;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ══════════════════════════════════════════════════════════════════════════
    // Khởi tạo
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearchFilter();
        setupSelectionListener();
        setActionButtonsDisabled(true);
        loadProblemsAsync();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Cấu hình TableView
    // ──────────────────────────────────────────────────────────────────────────

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setStyle("-fx-alignment: CENTER;");

        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        colSourceType.setCellValueFactory(new PropertyValueFactory<>("sourceType"));
        colSourceType.setStyle("-fx-alignment: CENTER;");

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setStyle("-fx-alignment: CENTER;");
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = switch (item) {
                        case "NEW"      -> "-fx-text-fill: #2563eb; -fx-font-weight: bold;";
                        case "ANALYZED" -> "-fx-text-fill: #16a34a; -fx-font-weight: bold;";
                        case "DONE"     -> "-fx-text-fill: #7c3aed; -fx-font-weight: bold;";
                        default         -> "";
                    };
                    setStyle("-fx-alignment: CENTER; " + color);
                }
            }
        });

        // Cột ngày tạo — Problem.createdAt có thể null nên cần format thủ công
        colCreatedAt.setCellValueFactory(data -> {
            Problem p = data.getValue();
            String txt = (p.getCreatedAt() != null)
                    ? p.getCreatedAt().format(FORMATTER)
                    : "-";
            return new SimpleStringProperty(txt);
        });

        filteredList = new FilteredList<>(masterList, p -> true);
        problemTable.setItems(filteredList);
        problemTable.setPlaceholder(
                new Label("Chưa có đề bài nào trong database."));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tìm kiếm realtime
    // ──────────────────────────────────────────────────────────────────────────

    private void setupSearchFilter() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(problem -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase();
                boolean titleMatch = problem.getTitle() != null
                        && problem.getTitle().toLowerCase().contains(lower);
                boolean typeMatch  = problem.getSourceType() != null
                        && problem.getSourceType().toLowerCase().contains(lower);
                return titleMatch || typeMatch;
            });
            statusLabel.setText("Tìm thấy " + filteredList.size() + " kết quả");
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Listener chọn dòng → hiển thị chi tiết
    // ──────────────────────────────────────────────────────────────────────────

    private void setupSelectionListener() {
        problemTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> {
                    if (newSel != null) {
                        populateDetail(newSel);
                        setActionButtonsDisabled(false);
                    } else {
                        clearDetail();
                        setActionButtonsDisabled(true);
                    }
                });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Load dữ liệu từ DB (async — tránh đơ UI)
    // ══════════════════════════════════════════════════════════════════════════

    private void loadProblemsAsync() {
        loadingLabel.setVisible(true);
        statusLabel.setText("Đang kết nối database...");
        btnRefresh.setDisable(true);

        Task<List<Problem>> task = new Task<>() {
            @Override
            protected List<Problem> call() {
                return problemService.getAllProblems();
            }
        };

        task.setOnSucceeded(e -> {
            List<Problem> result = task.getValue();
            masterList.setAll(result);
            loadingLabel.setVisible(false);
            btnRefresh.setDisable(false);
            statusLabel.setText("Tổng cộng " + masterList.size() + " đề bài");
        });

        task.setOnFailed(e -> {
            loadingLabel.setVisible(false);
            btnRefresh.setDisable(false);
            Throwable ex = task.getException();
            String msg = (ex.getCause() != null)
                    ? ex.getCause().getMessage()
                    : ex.getMessage();
            statusLabel.setText("⚠ Lỗi kết nối database");
            showError("Không thể tải danh sách đề bài",
                    "Kiểm tra kết nối SQL Server và thông tin trong application.properties.\n\nChi tiết lỗi:\n" + msg);
        });

        Thread worker = new Thread(task, "problem-list-loader");
        worker.setDaemon(true);
        worker.start();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Xử lý sự kiện các nút
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleRefresh() {
        masterList.clear();
        problemTable.getSelectionModel().clearSelection();
        clearDetail();
        searchField.clear();
        setActionButtonsDisabled(true);
        loadProblemsAsync();
    }

    @FXML
    private void handleOpenSourceFile() {
        Problem selected = problemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Chưa chọn đề", "Vui lòng chọn một đề trong danh sách trước.");
            return;
        }

        String sourcePath = selected.getSourcePath();
        if (sourcePath == null || sourcePath.isBlank()) {
            showInfo("Không có file gốc", "Đề bài này không có source_path để mở.");
            return;
        }

        File file = new File(sourcePath);
        if (!file.exists()) {
            showError("Không tìm thấy file", "File gốc không tồn tại trên máy: \n" + sourcePath);
            return;
        }

        try {
            if (!Desktop.isDesktopSupported()) {
                showError("Không hỗ trợ mở file", "Môi trường hiện tại không hỗ trợ mở file bằng Desktop.");
                return;
            }
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            showError("Mở file thất bại", "Không thể mở file gốc:\n" + ex.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Problem selected = problemTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa đề: \"" + selected.getTitle() + "\"?");
        confirm.setContentText(
                "Toàn bộ dữ liệu liên quan (testcase, kết quả chạy) cũng sẽ bị xóa.\n"
                        + "Hành động này không thể hoàn tác!");

        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;

            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() {
                    problemService.deleteProblem(selected.getId());
                    return null;
                }
            };

            deleteTask.setOnSucceeded(e -> {
                masterList.remove(selected);
                clearDetail();
                setActionButtonsDisabled(true);
                statusLabel.setText("Đã xóa \"" + selected.getTitle()
                        + "\" | Còn " + masterList.size() + " đề");
            });

            deleteTask.setOnFailed(e ->
                    showError("Xóa thất bại",
                            "Không thể xóa đề bài. Chi tiết:\n"
                                    + deleteTask.getException().getMessage()));

            Thread worker = new Thread(deleteTask, "problem-delete-task");
            worker.setDaemon(true);
            worker.start();
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helpers — Detail panel
    // ══════════════════════════════════════════════════════════════════════════

    private void populateDetail(Problem p) {
        detailTitle.setText(p.getTitle() != null ? p.getTitle() : "(Không có tiêu đề)");

        detailSourceType.setText(p.getSourceType() != null ? p.getSourceType() : "-");

        detailStatus.setText(p.getStatus() != null ? p.getStatus() : "-");

        detailCreatedAt.setText(p.getCreatedAt() != null
                ? p.getCreatedAt().format(FORMATTER) : "-");

        if (p.getSourcePath() != null && !p.getSourcePath().isBlank()) {
            File sourceFile = new File(p.getSourcePath());
            String fileName = sourceFile.getName();
            detailSourcePath.setText(!fileName.isBlank() ? fileName : p.getSourcePath());
            detailSourcePath.setTooltip(new Tooltip(p.getSourcePath()));
        } else {
            detailSourcePath.setText("(Không có file đính kèm)");
            detailSourcePath.setTooltip(null);
        }

        detailRawText.setText(p.getRawText() != null && !p.getRawText().isBlank()
                ? p.getRawText()
                : "This problem has no extracted text yet.");
    }

    private void clearDetail() {
        detailTitle.setText("Chọn một đề để xem chi tiết");
        detailSourceType.setText("-");
        detailStatus.setText("-");
        detailCreatedAt.setText("-");
        detailSourcePath.setText("-");
        detailSourcePath.setTooltip(null);
        detailRawText.setText("");
    }

    private void setActionButtonsDisabled(boolean disabled) {
        btnOpenSourceFile.setDisable(disabled);
        btnDelete.setDisable(disabled);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helpers — Dialog
    // ══════════════════════════════════════════════════════════════════════════

    private void showError(String title, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
