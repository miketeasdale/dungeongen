package com.mteasdale.dungeongen;

/**
 * Created by VDTS on 2017-02-20.
 */
public class Tile {
    char content;
    int region;

    public Tile() {
        // Initialize the tile with stone and no region.
        this.content = DungeonGen.STONE;
        this.region = -1;
    }

    public char getContent() {
        return content;
    }

    public void setContent(char content) {
        this.content = content;
    }

    public int getRegion() {
        return region;
    }

    public void setRegion(int region) {
        this.region = region;
    }
}
