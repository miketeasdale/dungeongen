package com.mteasdale.dungeongen;/**
 * Created by Michael Teasdale on 2/18/2017.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/main.fxml"));
            // Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root);
            stage.setTitle("Dungeon Generator");
            stage.setScene(scene);
            stage.show();
        } catch (IOException ioe) {
            //System.out.println("Unable to load UI file.");
            ioe.printStackTrace();
        }
    }
}
