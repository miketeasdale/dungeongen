package com.mteasdale.dungeongen;

/**
 * Created by VDTS on 2017-02-20.
 * Random number generator.
 */
public class Rng {
    public static int getRandomIn(int min, int max) {
        return (int) (Math.random() * (max - min)) + min;
    }

    public static boolean oneIn(int range) {
        return getRandomIn(1, range) == 1;
    }

}
