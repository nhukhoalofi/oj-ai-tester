package com.yourteam.ojaitester.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import java.net.URL;

public class MainLayoutController {

	@FXML private StackPane centerContainer;

	@FXML
	public void initialize() {
		showProblemList();
	}

	@FXML
	private void showProblemForm() {
		loadView("/fxml/problem-form.fxml");
	}

	@FXML
	private void showProblemList() {
		loadView("/fxml/problem-list.fxml");
	}

	@FXML
	private void showSubmissions() {
		loadView("/fxml/submission.fxml");
	}

	@FXML
	private void showAIAnalysis() {
		loadView("/fxml/ai-analysis.fxml");
	}

	@FXML
	private void showTestcases() {
		loadView("/fxml/testcases.fxml");
	}

	@FXML
	private void showEvaluation() {
		loadView("/fxml/evaluation-view.fxml");
	}

	private void loadView(String resourcePath) {
		try {
			URL resource = getClass().getResource(resourcePath);
			if (resource == null) {
				throw new IllegalStateException("Missing FXML resource: " + resourcePath);
			}

			Parent view = FXMLLoader.load(resource);
			centerContainer.getChildren().setAll(view);
		} catch (Exception e) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Navigation Error");
			alert.setHeaderText("Không thể tải màn hình");
			alert.setContentText("Failed to load: " + resourcePath + "\n\n" + e.getMessage());
			alert.showAndWait();
		}
	}

	private void showComingSoon(String featureName) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(featureName);
		alert.setHeaderText(null);
		alert.setContentText(featureName + " is under development.");
		alert.showAndWait();
	}
}
