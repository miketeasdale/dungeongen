package com.mteasdale.dungeongen.controllers;

import com.mteasdale.dungeongen.DungeonGen;
import com.mteasdale.dungeongen.TileMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);
    private final double WIDTH = 32;
    private final double HEIGHT = 32;
    private final int GRIDSIZE = 3; // Meters per grid. 
    private final int DWIDTH = 50; // Dungeon width in grids.
    private final int DLENGTH = 50; // Dungeon length in grids.
    private final int NUMROOMS = 50;
    private final int NUMTRIES = 3;

    GridPane gridPane = null;

    @FXML
    private void initialize(){
        DungeonGen dungeonGen = new DungeonGen(DWIDTH, DLENGTH, NUMROOMS, NUMTRIES);
        dungeonGen.generate();
        TileMap tileMap = dungeonGen.getTileMap();
        gridPane = new GridPane();
        for (int x = 0; x < DWIDTH * GRIDSIZE; x++) {
            for (int y = 0; y < DLENGTH * GRIDSIZE; y++) {
                Button button = new Button();
                button.setPrefWidth(WIDTH);
                button.setPrefHeight(HEIGHT);
                // Color button according to the contents of this map position.
                char content = tileMap.getContent(new Point2D(x/GRIDSIZE, y/GRIDSIZE));
                if (content == DungeonGen.STONE) {
                    button.setStyle("-fx-base: #000000;");
                }
                if (content == DungeonGen.OPEN) {
                    button.setStyle("-fx-base: #FFFFFF;");
                }
                button.setOnAction(event -> {
                        Button source = (Button) event.getSource();
                        LOG.info("Button pressed at ({},{})", GridPane.getColumnIndex(source),
                                GridPane.getRowIndex(source));
                        statLabel.setText("Button pressed at (" + GridPane.getColumnIndex(source) +
                        "," + GridPane.getRowIndex(source) + ")");
                });
                GridPane.setConstraints(button, x, y);
                gridPane.getChildren().add(button);
            }
        }
        scrollPane.setContent(gridPane);
    }

    @FXML
    private void takeAction() {
    }

    private Button getButtonAt(int row, int col) {
        Button button = null;
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                button = (Button) node;
                break;
            }
        }
        return button;
    }
}
