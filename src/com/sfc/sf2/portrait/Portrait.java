/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.portrait;


import java.awt.image.IndexColorModel;
import java.awt.image.DataBuffer;
import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.palette.Palette;
import java.awt.image.BufferedImage;

/**
 *
 * @author wiz
 */
public class Portrait {
    
    private Tile[] tiles;
    
    private int[][] eyeTiles;
    
    private int[][] mouthTiles;
    
    private BufferedImage image;

    public Tile[] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[] tiles) {
        this.tiles = tiles;
    }
    
    public int[][] getEyeTiles() {
        return eyeTiles;
    }

    public void setEyeTiles(int[][] eyeTiles) {
        this.eyeTiles = eyeTiles;
    }

    public int[][] getMouthTiles() {
        return mouthTiles;
    }

    public void setMouthTiles(int[][] mouthTiles) {
        this.mouthTiles = mouthTiles;
    }
    
    public Palette getPalette() {
        if (tiles == null || tiles.length == 0) {
            return null;
        }
        return tiles[0].getPalette();
    }
}
