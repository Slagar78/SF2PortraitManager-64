/*
 * Tile.java - 64 COLOR FINAL with setPixel method
 */
package com.sfc.sf2.graphics;

import com.sfc.sf2.palette.Palette;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;

public class Tile {

    public static final int PIXEL_WIDTH = 8;
    public static final int PIXEL_HEIGHT = 8;

    private int id;
    private Palette palette;
    private int[][] pixels = new int[PIXEL_HEIGHT][PIXEL_WIDTH];
    private BufferedImage indexedColorImage = null;

    private boolean highPriority = false;
    private boolean hFlip = false;
    private boolean vFlip = false;
    private int occurrences = 1;

    public Tile() {
        this.id = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                pixels[y][x] = 0;
            }
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Palette getPalette() { return palette; }
    public void setPalette(Palette palette) {
        this.palette = palette;
        clearIndexedColorImage();
    }

    public int[][] getPixels() { return pixels; }
    public void setPixels(int[][] pixels) {
        this.pixels = pixels;
        clearIndexedColorImage();
    }

    // ← Добавленный метод, которого не хватало
    public void setPixel(int x, int y, int color) {
        if (x >= 0 && x < 8 && y >= 0 && y < 8) {
            pixels[y][x] = color;
        }
    }

    public boolean isHighPriority() { return highPriority; }
    public void setHighPriority(boolean b) { highPriority = b; }
    public boolean isHFlip() { return hFlip; }
    public void setHFlip(boolean b) { hFlip = b; }
    public boolean isVFlip() { return vFlip; }
    public void setVFlip(boolean b) { vFlip = b; }
    public int getOccurrences() { return occurrences; }
    public void setOccurrences(int o) { occurrences = o; }

    public BufferedImage getIndexedColorImage() {
        if (indexedColorImage == null && palette != null && palette.getIcm() != null) {
            IndexColorModel icm = palette.getIcm();
            indexedColorImage = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_INDEXED, icm);
            byte[] data = ((DataBufferByte) indexedColorImage.getRaster().getDataBuffer()).getData();

            // column-major порядок — точно как раньше
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    data[y * 8 + x] = (byte) pixels[y][x];
                }
            }
        }
        return indexedColorImage;
    }

    public void clearIndexedColorImage() {
        indexedColorImage = null;
    }

    // Заглушки для методов, которые могут вызываться из других классов
    public static Tile hFlip(Tile tile) { return tile; }
    public static Tile vFlip(Tile tile) { return tile; }
    public static Tile paletteSwap(Tile tile, Palette newPalette) {
        tile.setPalette(newPalette);
        return tile;
    }

    public Tile getHFlipped() { return this; }
    public Tile getVFlipped() { return this; }
    public Tile getPaletteSwapped(int[] mapping) { return this; }
    public static Tile emptyTile() { return new Tile(); }
}