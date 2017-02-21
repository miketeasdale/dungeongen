package com.mteasdale.dungeongen;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by VDTS on 2017-02-20.
 */
public class TileMap {
    List<List<Tile>> map = new ArrayList<>();

    public TileMap(int width, int height) {
        for (int i = 0; i < height; i++) {
            List<Tile> mapRow = new ArrayList<>(width);
            for (int j = 0; j < width; j++) {
                mapRow.add(new Tile());
            }
            map.add(mapRow);
        }
    }

    public void setContent(Point2D pos, char content) {
        map.get((int) pos.getY()).get((int) pos.getX()).setContent(content);
    }

    public char getContent(Point2D pos) {
        return map.get((int) pos.getY()).get((int) pos.getX()).getContent();
    }

    public void setRegion(Point2D pos, int region) {
        map.get((int) pos.getY()).get((int) pos.getX()).setRegion(region);
    }

    public int getRegion(Point2D pos) {
        return map.get((int) pos.getY()).get((int) pos.getX()).getRegion();
    }

    public void setTile(int row, int col, Tile tile) {
        map.get(row).add(col, tile);
    }

    public Tile getTile(int row, int col) {
        return map.get(row).get(col);
    }
}
