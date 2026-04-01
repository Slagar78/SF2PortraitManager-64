/*
 * Palette64.java - extends Palette to avoid type conflict
 */
package com.sfc.sf2.palette;

import com.sfc.sf2.graphics.Tile;
import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.lang.reflect.Field;

public class Palette64 extends Palette {

    public Palette64(String name, Color[] colors) {
        super(name, colors);
    }

    public Palette64(Color[] colors) {
        this("64color_portrait", colors);
    }

    @Override
    public int getColorsCount() {
        return 64;
    }

    public void applyToTile(Tile tile) {
        if (tile == null) return;
        try {
            Field f = Tile.class.getDeclaredField("palette");
            f.setAccessible(true);
            f.set(tile, this);
        } catch (Exception e) {
            System.err.println("Failed to apply Palette64 to Tile: " + e.getMessage());
        }
    }
}