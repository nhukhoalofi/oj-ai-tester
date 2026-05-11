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
}