package com.mteasdale.dungeongen.controllers;

import com.mteasdale.dungeongen.components.GridButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 * Created by Michael Teasdale on 2/18/2017.
 */
public class MainController {
    @FXML
    private Label statLabel;
    @FXML
    private Button actionButton;
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private void initialize(){
        GridPane gridPane = new GridPane();
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 49; y++) {
                GridButton gridButton = new GridButton(x, y);
                gridButton.getButton().addEventHandler();
                GridPane.setConstraints(gridButton.getButton(), x, y);
                gridPane.getChildren().add(gridButton.getButton());
            }
        }
        scrollPane.setContent(gridPane);
    }

    @FXML
    private void takeAction() {

    }
}
