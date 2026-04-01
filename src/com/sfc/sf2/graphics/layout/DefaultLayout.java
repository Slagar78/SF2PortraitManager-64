/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.graphics.layout;

import com.sfc.sf2.graphics.Tile;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author wiz
 */
public class DefaultLayout extends JPanel {
    
    private static final int DEFAULT_TILES_PER_ROW = 32;
    
    private int tilesPerRow = DEFAULT_TILES_PER_ROW;
    private Tile[] tiles;
    
    BufferedImage currentImage;
    private boolean redraw = true;
    
    private int displaySize = 1;
    private boolean showGrid = false;
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);   
        g.drawImage(buildImage(), 0, 0, this);       
    }
    
    public BufferedImage buildImage(){
        if (redraw) {
            if (tiles == null || tiles.length == 0) {
                currentImage = null;
            } else {
                currentImage = buildImage(this.tiles,this.tilesPerRow);
                setSize(currentImage.getWidth(), currentImage.getHeight());
                if (showGrid) { drawGrid(currentImage); }
            }
            redraw = false;
        }
        return currentImage;
    }
    
    public BufferedImage buildImage(Tile[] tiles, int tilesPerRow){
        int imageHeight = (tiles.length/tilesPerRow)*8;
        if(tiles.length%tilesPerRow!=0){
            imageHeight+=8;
        }
        if(redraw){
            currentImage = new BufferedImage(tilesPerRow*8, imageHeight , BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = currentImage.getGraphics();
            int i=0;
            int j=0;
            while(i*tilesPerRow+j<tiles.length){
                while(j<tilesPerRow && i*tilesPerRow+j<tiles.length){
                    graphics.drawImage(tiles[i*tilesPerRow+j].getIndexedColorImage(), j*8, i*8, null);
                    j++;
                }
                j=0;
                i++;
            }
            redraw = false;
        }
        currentImage = resize(currentImage);
        return currentImage;
    }
    
    private void drawGrid(BufferedImage image) {
        Graphics graphics = image.getGraphics();
        graphics.setColor(Color.BLACK);
        int x = 0;
        int y = 0;
        while (x < image.getWidth()) {
            graphics.drawLine(x, 0, x, image.getHeight());
            x += 8*displaySize;
        }
        graphics.drawLine(x-1, 0, x-1, image.getHeight());
        while (y < image.getHeight()) {
            graphics.drawLine(0, y, image.getWidth(), y);
            y += 8*displaySize;
        }
        graphics.drawLine(0, y-1, image.getWidth(), y-1);
        graphics.dispose();
    }
    
    public void resize(int size){
        this.displaySize = size;
        currentImage = resize(currentImage);
        this.redraw = true;
    }
    
    private BufferedImage resize(BufferedImage image){
        if (displaySize == 1)
            return image;
        BufferedImage newImage = new BufferedImage(image.getWidth()*displaySize, image.getHeight()*displaySize, BufferedImage.TYPE_INT_ARGB);
        Graphics g = newImage.getGraphics();
        g.drawImage(image, 0, 0, image.getWidth()*displaySize, image.getHeight()*displaySize, null);
        g.dispose();
        return newImage;
    }  
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }
    
    public Tile[] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[] tiles) {
        this.tiles = tiles;
        redraw = true;
    }
    
    public int getTilesPerRow() {
        return tilesPerRow;
    }

    public void setTilesPerRow(int tilesPerRow) {
        this.tilesPerRow = tilesPerRow;
        redraw = true;
    }

    public int getDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(int displaySize) {
        this.displaySize = displaySize;
        redraw = true;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        redraw = true;
    } 
}
