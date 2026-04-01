/*
 * GraphicsManager.java - Адаптированная версия для 64-цветных портретов
 */
package com.sfc.sf2.graphics;

import com.sfc.sf2.graphics.io.RawImageManager;
import com.sfc.sf2.palette.Palette;
import com.sfc.sf2.palette.PaletteManager;
import com.sfc.sf2.portrait.io.DisassemblyManager;
import java.util.logging.Logger;

/**
 *
 * @author wiz
 */
public class GraphicsManager {
    
    private static final Logger LOG = Logger.getLogger(GraphicsManager.class.getName());
    
    public static final int COMPRESSION_NONE = 0;
    public static final int COMPRESSION_BASIC = 1;
    public static final int COMPRESSION_STACK = 2;    
       
    private PaletteManager paletteManager = new PaletteManager();
    private Tile[] tiles;

    public Tile[] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[] tiles) {
        this.tiles = tiles;
    }
       
    // Этот метод теперь почти не используется для портретов
    public void importDisassembly(String paletteFilePath, String graphicsFilePath, int compression){
        LOG.entering(LOG.getName(),"importDisassembly");
        LOG.warning("importDisassembly with 3 params is deprecated for portraits. Use PortraitManager instead.");
        LOG.exiting(LOG.getName(),"importDisassembly");
    }
    
    public void exportDisassembly(String graphicsFilePath, int compression){
        LOG.entering(LOG.getName(),"exportDisassembly");
        LOG.warning("exportDisassembly with compression is deprecated for portraits.");
        LOG.exiting(LOG.getName(),"exportDisassembly");        
    }   
    
    public void importRom(String romFilePath, String paletteOffset, String paletteLength, String graphicsOffset, String graphicsLength, int compression){
        LOG.entering(LOG.getName(),"importOriginalRom");
        LOG.warning("importRom not supported in 64-color portrait mode.");
        LOG.exiting(LOG.getName(),"importOriginalRom");
    }
    
    public void exportRom(String originalRomFilePath, String graphicsOffset, int compression){
        LOG.entering(LOG.getName(),"exportOriginalRom");
        LOG.warning("exportRom not supported in 64-color portrait mode.");
        LOG.exiting(LOG.getName(),"exportOriginalRom");        
    }      
    
    public int importPng(String filepath){
        LOG.entering(LOG.getName(),"importPng");
        tiles = RawImageManager.importImage(filepath);
        if (tiles != null && tiles.length > 0 && tiles[0].getPalette() != null) {
            paletteManager.setPalette(tiles[0].getPalette());
        }
        int tileWidth = RawImageManager.getImportedImageTileWidth();
        LOG.exiting(LOG.getName(),"importPng");
        return tileWidth;
    }
    
    public void exportPng(String filepath, int tilesPerRow){
        LOG.entering(LOG.getName(),"exportPng");
        RawImageManager.exportImage(tiles, filepath, tilesPerRow, RawImageManager.FILE_FORMAT_PNG);
        LOG.exiting(LOG.getName(),"exportPng");       
    }    
    
    public int importGif(String filepath){
        LOG.entering(LOG.getName(),"importGif");
        tiles = RawImageManager.importImage(filepath);
        if (tiles != null && tiles.length > 0 && tiles[0].getPalette() != null) {
            paletteManager.setPalette(tiles[0].getPalette());
        }
        int tileWidth = RawImageManager.getImportedImageTileWidth();
        LOG.exiting(LOG.getName(),"importGif");
        return tileWidth;
    }
    
    public void exportGif(String filepath, int tilesPerRow){
        LOG.entering(LOG.getName(),"exportGif");
        RawImageManager.exportImage(tiles, filepath, tilesPerRow, RawImageManager.FILE_FORMAT_GIF);
        LOG.exiting(LOG.getName(),"exportGif");       
    }
       
    // Заглушка для метода с layout (не используется в портретах)
    public void importDisassemblyWithLayout(String baseTilesetFilePath, 
            String palette1FilePath, String palette1Offset,
            String palette2FilePath, String palette2Offset,
            String palette3FilePath, String palette3Offset,
            String palette4FilePath, String palette4Offset,
            String tileset1FilePath, String tileset1Offset,
            String tileset2FilePath, String tileset2Offset,
            String layoutFilePath, int compression){
        LOG.warning("importDisassemblyWithLayout not supported in portrait mode.");
    }
    
    public void exportTilesAndLayout(String palettePath, String tilesPath, String layoutPath, 
            String graphicsOffset, int compression, int palette){
        LOG.warning("exportTilesAndLayout not supported in portrait mode.");
    }
}