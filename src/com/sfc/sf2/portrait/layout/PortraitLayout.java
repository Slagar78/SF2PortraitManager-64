/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.portrait.layout;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.portrait.gui.PortraitTableModel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import javax.swing.JPanel;

/**
 *
 * @author wiz
 */
public class PortraitLayout extends JPanel  implements MouseListener, MouseMotionListener{
    
    private static final int DEFAULT_TILES_PER_ROW = 8;
    
    private int tilesPerRow = DEFAULT_TILES_PER_ROW;
    private Tile[] tiles;
    
    private PortraitTableModel eyeAnimTable;
    private PortraitTableModel mouthAnimTable;
    
    private boolean blinking = false;
    private boolean speaking = false;
    
    private int selectedEyeTile = -1;
    private int selectedMouthTile = -1;
    
    private boolean showGrid = false;
    private boolean redraw = true;
    private BufferedImage currentImage = null;
    private BufferedImage selectedTileImage = null;
    
    private int displaySize = 2;
    
    public PortraitLayout() {
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
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
            for(int i=0;i<8;i++){
                for(int j=0;j<8;j++){
                    int tileID = i+j*8;
                    if (blinking) {
                        int[][] eyeTableData = eyeAnimTable.getTableData();
                        for (int b = 0; b < eyeTableData.length; b++) {
                            if (eyeTableData[b][0] == i && eyeTableData[b][1] == j) {
                                tileID = eyeTableData[b][2]+eyeTableData[b][3]*8;
                                break;
                            }
                        }
                    }
                    if (speaking) {
                        int[][] mouthTableData = mouthAnimTable.getTableData();
                        for (int m = 0; m < mouthTableData.length; m++) {
                            if (mouthTableData[m][0] == i && mouthTableData[m][1] == j) {
                                tileID = mouthTableData[m][2]+mouthTableData[m][3]*8;
                                break;
                            }
                        }
                    }
                    graphics.drawImage(tiles[tileID].getIndexedColorImage(), i*8, j*8, null);
                }
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
    
    public void updateDisplay(){
        selectedTileImage = null;
        this.redraw = true;
    }  
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }
    
        public Tile[] getTiles() {
        return tiles;
    }
    
    public int getDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(int displaySize) {
        if (this.displaySize != displaySize) {
            this.displaySize = displaySize;
            selectedTileImage = null;
            redraw = true;
        }
    }

    public void setTiles(Tile[] tiles) {
        this.tiles = tiles;
    }
    
    public int getTilesPerRow() {
        return tilesPerRow;
    }

    public void setTilesPerRow(int tilesPerRow) {
        this.tilesPerRow = tilesPerRow;
    }

    public PortraitTableModel getEyeAnimTable() {
        return eyeAnimTable;
    }

    public void setEyeAnimTable(PortraitTableModel eyeAnimTable) {
        this.eyeAnimTable = eyeAnimTable;
    }

    public PortraitTableModel getMouthAnimTable() {
        return mouthAnimTable;
    }

    public void setMouthAnimTable(PortraitTableModel mouthAnimTable) {
        this.mouthAnimTable = mouthAnimTable;
    }

    public int getSelectedEyeTile() {
        return selectedEyeTile;
    }

    public void setSelectedEyeTile(int selectedEyeTile) {
        this.selectedEyeTile = selectedEyeTile;
        this.selectedMouthTile = -1;
    }

    public int getSelectedMouthTile() {
        return selectedMouthTile;
    }

    public void setSelectedMouthTile(int selectedMouthTile) {
        this.selectedMouthTile = selectedMouthTile;
        this.selectedEyeTile = -1;
    }

    public boolean getBlinking() {
        return blinking;
    }

    public void setBlinking(boolean blinking) {
        if (this.blinking != blinking) {
            this.blinking = blinking;
            redraw = true;
        }
    }

    public boolean getspeaking() {
        return speaking;
    }

    public void setSpeaking(boolean speaking) {
        if (this.speaking != speaking) {
            this.speaking = speaking;
            redraw = true;
        }
    }

    public boolean getDrawGrid() {
        return showGrid;
    }

    public void setDrawGrid(boolean drawGrid) {
        if (this.showGrid != drawGrid) {
            this.showGrid = drawGrid;
            currentImage = null;
            redraw = true;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }
    @Override
    public void mouseEntered(MouseEvent e) {

    }
    @Override
    public void mouseExited(MouseEvent e) {

    }
    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX() / (displaySize * 8);
        int y = e.getY() / (displaySize * 8);  
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                if(selectedEyeTile>=0){
                    if(x<6){
                        eyeAnimTable.getTableData()[selectedEyeTile][0] = x;
                        eyeAnimTable.getTableData()[selectedEyeTile][1] = y;
                        eyeAnimTable.fireTableCellUpdated(selectedEyeTile, 0);
                        eyeAnimTable.fireTableCellUpdated(selectedEyeTile, 1);
                    }else{
                        eyeAnimTable.getTableData()[selectedEyeTile][2] = x;
                        eyeAnimTable.getTableData()[selectedEyeTile][3] = y;
                        eyeAnimTable.fireTableCellUpdated(selectedEyeTile, 2);
                        eyeAnimTable.fireTableCellUpdated(selectedEyeTile, 3);
                    }
                    //eyeAnimTable.fireTableDataChanged();
                    eyeAnimTable.fireTableCellUpdated(y, y);
                }
                if(selectedMouthTile>=0){
                    if(x<6){
                        mouthAnimTable.getTableData()[selectedMouthTile][0] = x;
                        mouthAnimTable.getTableData()[selectedMouthTile][1] = y;
                        mouthAnimTable.fireTableCellUpdated(selectedMouthTile, 0);
                        mouthAnimTable.fireTableCellUpdated(selectedMouthTile, 1);
                    }else{
                        mouthAnimTable.getTableData()[selectedMouthTile][2] = x;
                        mouthAnimTable.getTableData()[selectedMouthTile][3] = y;
                        mouthAnimTable.fireTableCellUpdated(selectedMouthTile, 2);
                        mouthAnimTable.fireTableCellUpdated(selectedMouthTile, 3);
                    }
                    //mouthAnimTable.fireTableDataChanged();
                }
                break;
            case MouseEvent.BUTTON2:

                break;
            case MouseEvent.BUTTON3:
                break;
            default:
                break;
        } 
        selectedTileImage = null;
        redraw = true;
        this.revalidate();
        this.repaint();
        System.out.println("Portrait press "+e.getButton()+" "+x+" - "+y);
    }
    @Override
    public void mouseReleased(MouseEvent e) {
       
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        
    }

    
}
