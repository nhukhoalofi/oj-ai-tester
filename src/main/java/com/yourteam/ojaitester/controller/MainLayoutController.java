package com.yourteam.ojaitester.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.io.IOException;

public class MainLayoutController {
    @FXML
    private BorderPane borderPane;
    @FXML
    private VBox sidebarVBox;

    @FXML
    public void initialize() {
        // Make menu items clickable
        setupMenuItems();
    }

    private void setupMenuItems() {
        if (sidebarVBox == null) return;

        sidebarVBox.getChildren().forEach(node -> {
            if (node instanceof Label) {
                Label label = (Label) node;
                String text = label.getText();

                label.setStyle("-fx-text-fill: white; -fx-cursor: hand;");
                label.setOnMouseEntered(e -> 
                    label.setStyle("-fx-text-fill: #3b82f6; -fx-cursor: hand;")
                );
                label.setOnMouseExited(e -> 
                    label.setStyle("-fx-text-fill: white; -fx-cursor: hand;")
                );

                label.setOnMouseClicked(e -> handleMenuClick(text));
            }
        });
    }

    private void handleMenuClick(String menuName) {
        try {
            String fxmlFile = switch (menuName) {
                case "Add Problem" -> "/fxml/problem-form.fxml";
                case "Submissions" -> "/fxml/submission.fxml";
                default -> null;
            };

            if (fxmlFile != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                borderPane.setCenter(loader.load());
            }
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + e.getMessage());
        }
    }
}