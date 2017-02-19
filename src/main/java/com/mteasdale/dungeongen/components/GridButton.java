package com.mteasdale.dungeongen.components;

import javafx.scene.control.Button;

/**
 * Created by Michael Teasdale on 2/18/2017.
 */
public class GridButton {
    private Button button;
    private Integer x;
    private Integer y;
    private static final double WIDTH = 32;
    private static final double HEIGHT = 32;

    public GridButton(int x, int y) {
        button = new Button();
        button.setPrefWidth(WIDTH);
        button.setPrefHeight(HEIGHT);
        this.x = x;
        this.y = y;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}
