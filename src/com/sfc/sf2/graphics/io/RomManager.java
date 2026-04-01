/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.graphics.io;

import com.sfc.sf2.graphics.GraphicsManager;
import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.graphics.compressed.BasicGraphicsDecoder;
import com.sfc.sf2.graphics.compressed.BasicGraphicsEncoder;
import com.sfc.sf2.graphics.compressed.StackGraphicsDecoder;
import com.sfc.sf2.graphics.compressed.StackGraphicsEncoder;
import com.sfc.sf2.graphics.uncompressed.UncompressedGraphicsDecoder;
import com.sfc.sf2.graphics.uncompressed.UncompressedGraphicsEncoder;
import com.sfc.sf2.palette.Palette;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 *
 * @author wiz
 */
public class RomManager {
    
    private static File romFile;  
    private static byte[] romData;

    private static final Logger LOG = Logger.getLogger(RomManager.class.getName());
    
    public static Tile[] importRom(String romFilePath, String graphicsOffset, String graphicsLength, int compression, Palette palette){
        LOG.entering(LOG.getName(),"importRom");
        RomManager.openFile(romFilePath);
        int offset = Integer.parseInt(graphicsOffset,16);
        int length = Integer.parseInt(graphicsLength);
        Tile[] tiles = RomManager.parseGraphics(offset,length,compression, palette);        
        LOG.exiting(LOG.getName(),"importRom");
        return tiles;
    }
    
    public static void exportRom(Tile[] tiles, String romFilePath, String graphicsOffset, int compression){
        LOG.entering(LOG.getName(),"exportRom");
        RomManager.produceGraphics(tiles, compression);
        int offset = Integer.parseInt(graphicsOffset,16);
        RomManager.writeFile(romFilePath, offset, compression);
        LOG.exiting(LOG.getName(),"exportRom");        
    }    
    
    private static void openFile(String romFilePath){
        try {
            LOG.entering(LOG.getName(),"openFile");
            LOG.fine("ROM file path : " + romFilePath);
            romFile = new File(romFilePath);
            romData = Files.readAllBytes(Paths.get(romFile.getAbsolutePath()));
            LOG.exiting(LOG.getName(),"openFile");
        } catch (IOException ex) {
            LOG.throwing(LOG.getName(),"openFile", ex);
        }
    }
    
    private static Tile[] parseGraphics(int graphicsOffset, int graphicsLength, int compression, Palette palette){
        LOG.entering(LOG.getName(),"parseGraphics");
        byte[] data = Arrays.copyOfRange(romData,graphicsOffset,graphicsOffset+graphicsLength);        
        Tile[] tiles = null;
        switch(compression){
            case GraphicsManager.COMPRESSION_NONE:
                tiles = UncompressedGraphicsDecoder.decodeUncompressedGraphics(data, palette);
                break;
            case GraphicsManager.COMPRESSION_BASIC:
                tiles = BasicGraphicsDecoder.decodeBasicGraphics(data, palette);
                break;
            case GraphicsManager.COMPRESSION_STACK:
                tiles = new StackGraphicsDecoder().decodeStackGraphics(data, palette);
                break;

        }
        LOG.exiting(LOG.getName(),"parseGraphics");
        return tiles;
    }

    private static void produceGraphics(Tile[] tiles, int compression) {
        LOG.entering(LOG.getName(),"produceGraphics");
        switch(compression){
            case GraphicsManager.COMPRESSION_NONE:
                UncompressedGraphicsEncoder.produceGraphics(tiles);
                break;
            case GraphicsManager.COMPRESSION_BASIC:
                BasicGraphicsEncoder.produceGraphics(tiles);
                break;
            case GraphicsManager.COMPRESSION_STACK:
                StackGraphicsEncoder.produceGraphics(tiles);
                break;
        }        
        LOG.exiting(LOG.getName(),"produceGraphics");
    }    
  
    private static void writeFile(String romFilePath, int offset, int compression){
        try {
            LOG.entering(LOG.getName(),"writeFile");
            romFile = new File(romFilePath);
            Path romPath = Paths.get(romFile.getAbsolutePath());
            romData = Files.readAllBytes(romPath);
            byte[] newGraphicsFileBytes = null;
            switch(compression){
                case GraphicsManager.COMPRESSION_NONE:
                    newGraphicsFileBytes = UncompressedGraphicsEncoder.getNewGraphicsFileBytes(); 
                    break;
                case GraphicsManager.COMPRESSION_BASIC:
                    newGraphicsFileBytes = BasicGraphicsEncoder.getNewGraphicsFileBytes(); 
                    break;
                case GraphicsManager.COMPRESSION_STACK:
                    newGraphicsFileBytes = StackGraphicsEncoder.getNewGraphicsFileBytes(); 
                    break;
            }
            System.arraycopy(newGraphicsFileBytes, 0, romData, offset, newGraphicsFileBytes.length);
            Files.write(romPath,romData);
            LOG.fine(romData.length + " bytes into " + romFilePath);  
            LOG.entering(LOG.getName(),"writeFile");
        } catch (IOException ex) {
            LOG.throwing(LOG.getName(),"writeFile", ex);
        }
    }    
    
}
