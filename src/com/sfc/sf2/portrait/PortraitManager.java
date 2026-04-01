/*
 * PortraitManager.java - 64 COLOR FINAL (PNG + GIF support)
 */
package com.sfc.sf2.portrait;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

import com.sfc.sf2.graphics.GraphicsManager;
import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.portrait.io.DisassemblyManager;
import com.sfc.sf2.portrait.io.MetaManager;
import com.sfc.sf2.graphics.io.RawImageManager;
import com.sfc.sf2.palette.Palette64;

public class PortraitManager {
       
    private final GraphicsManager graphicsManager = new GraphicsManager();
    private Tile[] tiles;
    private Portrait portrait;
    
    private int getUniqueColorCount(BufferedImage img) {
        Set<Integer> set = new HashSet<>();
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                set.add(img.getRGB(x, y) & 0x00FFFFFF);
            }
        }
        return set.size();
    }
    
    public void importPng(String filepath, String metadataPath) {
        System.out.println("=== Importing PNG - 64 COLOR FINAL ===");
        
        try {
            BufferedImage original = ImageIO.read(new File(filepath));
            
            int colorCount = getUniqueColorCount(original);
            System.out.println("Original unique colors : " + colorCount);
            System.out.println("Image size             : " + original.getWidth() + "x" + original.getHeight());

            Tile[] newTiles = create64ColorTiles(original);

            portrait = new Portrait();
            portrait.setTiles(newTiles);

            try {
                MetaManager.importMetadata(portrait, getMetadataFullPath(filepath, metadataPath));
            } catch (Exception ignored) {
                System.out.println("No .meta file (ignored)");
            }

            this.tiles = portrait.getTiles();
            graphicsManager.setTiles(portrait.getTiles());

            System.out.println("✅ 64-color PNG import completed successfully!");

        } catch (Exception e) {
            System.err.println("Error during PNG import: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Tile[] create64ColorTiles(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int tilesX = w / 8;
        int tilesY = h / 8;
        int totalTiles = tilesX * tilesY;

        System.out.println("Creating " + totalTiles + " tiles...");

        int[] paletteData = new int[64];
        paletteData[0] = 0x00000000;

        Set<Integer> unique = new HashSet<>();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y) & 0x00FFFFFF;
                if (unique.size() < 63) unique.add(rgb);
            }
        }
        int idx = 1;
        for (int color : unique) {
            if (idx < 64) paletteData[idx++] = color | 0xFF000000;
        }
        while (idx < 64) paletteData[idx++] = 0xFF000000;

        IndexColorModel icm64 = new IndexColorModel(6, 64, paletteData, 0, true, 0, DataBufferByte.TYPE_BYTE);

        Color[] colors = new Color[64];
        for (int i = 0; i < 64; i++) {
            colors[i] = new Color(paletteData[i], true);
        }
        Palette64 sharedPalette = new Palette64(colors);

        Tile[] tilesArr = new Tile[totalTiles];
        int tileIndex = 0;

        for (int ty = 0; ty < tilesY; ty++) {
            for (int tx = 0; tx < tilesX; tx++) {
                Tile tile = new Tile();

                int[][] tilePixels = new int[8][8];
                for (int py = 0; py < 8; py++) {
                    for (int px = 0; px < 8; px++) {
                        int x = tx * 8 + px;
                        int y = ty * 8 + py;
                        int rgb = src.getRGB(x, y) & 0x00FFFFFF;

                        int colorIndex = 0;
                        for (int c = 0; c < 64; c++) {
                            if ((paletteData[c] & 0x00FFFFFF) == rgb) {
                                colorIndex = c;
                                break;
                            }
                        }
                        tilePixels[py][px] = colorIndex;
                    }
                }

                tile.setPixels(tilePixels);
                
                try {
                    java.lang.reflect.Field f = Tile.class.getDeclaredField("palette");
                    f.setAccessible(true);
                    f.set(tile, sharedPalette);
                } catch (Exception ex) {
                    System.err.println("Reflection setPalette failed: " + ex.getMessage());
                }

                BufferedImage tileImage = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_INDEXED, icm64);
                byte[] data = ((DataBufferByte) tileImage.getRaster().getDataBuffer()).getData();
                for (int py = 0; py < 8; py++) {
                    for (int px = 0; px < 8; px++) {
                        data[py * 8 + px] = (byte) tilePixels[py][px];
                    }
                }

                try {
                    java.lang.reflect.Field field = Tile.class.getDeclaredField("indexedColorImage");
                    field.setAccessible(true);
                    field.set(tile, tileImage);
                } catch (Exception ignored) {
                    tile.clearIndexedColorImage();
                }

                tilesArr[tileIndex++] = tile;
            }
        }

        System.out.println("All 64 tiles created with shared 64-color palette");
        return tilesArr;
    }
    
    // ==================== РУЧНОЙ ЭКСПОРТ PNG ====================
    public void exportPng(String filepath, String metadataPath){
        System.out.println("Exporting PNG (manual assembly)...");
        
        if (portrait == null || portrait.getTiles() == null || portrait.getTiles().length == 0) {
            System.err.println("No tiles to export to PNG");
            return;
        }

        Tile[] tiles = portrait.getTiles();
        Palette64 palette = null;

        if (tiles.length > 0 && tiles[0] != null) {
            try {
                java.lang.reflect.Field f = Tile.class.getDeclaredField("palette");
                f.setAccessible(true);
                Object p = f.get(tiles[0]);
                if (p instanceof Palette64) palette = (Palette64) p;
            } catch (Exception ignored) {}
        }

        if (palette == null) {
            System.err.println("Palette not found for PNG export");
            return;
        }

        Color[] colors = palette.getColors();
        int[] paletteData = new int[64];
        for (int i = 0; i < 64; i++) {
            paletteData[i] = colors[i].getRGB();
        }

        IndexColorModel icm64 = new IndexColorModel(6, 64, paletteData, 0, true, 0, DataBufferByte.TYPE_BYTE);

        BufferedImage finalImage = new BufferedImage(64, 64, BufferedImage.TYPE_BYTE_INDEXED, icm64);
        byte[] imageData = ((DataBufferByte) finalImage.getRaster().getDataBuffer()).getData();

        int tileIndex = 0;
        for (int ty = 0; ty < 8; ty++) {
            for (int tx = 0; tx < 8; tx++) {
                Tile tile = tiles[tileIndex++];
                if (tile == null) continue;

                int[][] pixels = tile.getPixels();
                if (pixels == null) continue;

                int baseX = tx * 8;
                int baseY = ty * 8;

                for (int py = 0; py < 8; py++) {
                    for (int px = 0; px < 8; px++) {
                        int destIndex = (baseY + py) * 64 + (baseX + px);
                        imageData[destIndex] = (byte) pixels[py][px];
                    }
                }
            }
        }

        try {
            ImageIO.write(finalImage, "png", new File(filepath));
            System.out.println("PNG exported successfully: " + filepath);
            System.out.println("File size: " + new File(filepath).length() + " bytes");
        } catch (Exception e) {
            System.err.println("Error saving PNG: " + e.getMessage());
            e.printStackTrace();
        }

        MetaManager.exportMetadata(portrait, getMetadataFullPath(filepath, metadataPath));
    }
    
    // ==================== GIF ИМПОРТ ====================
    public void importGif(String filepath, String metadataPath){
        System.out.println("Importing GIF ...");
        try {
            BufferedImage gifImage = ImageIO.read(new File(filepath));
            
            if (gifImage == null) {
                System.err.println("Failed to read GIF image");
                return;
            }

            Tile[] newTiles = create64ColorTiles(gifImage);

            portrait = new Portrait();
            portrait.setTiles(newTiles);

            try {
                MetaManager.importMetadata(portrait, getMetadataFullPath(filepath, metadataPath));
            } catch (Exception ignored) {
                System.out.println("No .meta file (ignored)");
            }

            this.tiles = portrait.getTiles();
            graphicsManager.setTiles(portrait.getTiles());

            System.out.println("GIF imported successfully as 64-color portrait!");
        } catch (Exception e) {
            System.err.println("Error during GIF import: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ==================== GIF ЭКСПОРТ (настоящий GIF) ====================
    public void exportGif(String filepath, String metadataPath){
        System.out.println("Exporting GIF ...");
        
        if (portrait == null || portrait.getTiles() == null || portrait.getTiles().length == 0) {
            System.err.println("No tiles to export to GIF");
            return;
        }

        Tile[] tiles = portrait.getTiles();
        Palette64 palette = null;

        if (tiles.length > 0 && tiles[0] != null) {
            try {
                java.lang.reflect.Field f = Tile.class.getDeclaredField("palette");
                f.setAccessible(true);
                Object p = f.get(tiles[0]);
                if (p instanceof Palette64) palette = (Palette64) p;
            } catch (Exception ignored) {}
        }

        if (palette == null) {
            System.err.println("Palette not found for GIF export");
            return;
        }

        Color[] colors = palette.getColors();
        int[] paletteData = new int[64];
        for (int i = 0; i < 64; i++) {
            paletteData[i] = colors[i].getRGB();
        }

        IndexColorModel icm64 = new IndexColorModel(6, 64, paletteData, 0, true, 0, DataBufferByte.TYPE_BYTE);

        BufferedImage finalImage = new BufferedImage(64, 64, BufferedImage.TYPE_BYTE_INDEXED, icm64);
        byte[] imageData = ((DataBufferByte) finalImage.getRaster().getDataBuffer()).getData();

        int tileIndex = 0;
        for (int ty = 0; ty < 8; ty++) {
            for (int tx = 0; tx < 8; tx++) {
                Tile tile = tiles[tileIndex++];
                if (tile == null) continue;

                int[][] pixels = tile.getPixels();
                if (pixels == null) continue;

                int baseX = tx * 8;
                int baseY = ty * 8;

                for (int py = 0; py < 8; py++) {
                    for (int px = 0; px < 8; px++) {
                        int destIndex = (baseY + py) * 64 + (baseX + px);
                        imageData[destIndex] = (byte) pixels[py][px];
                    }
                }
            }
        }

        try {
            ImageIO.write(finalImage, "gif", new File(filepath));
            System.out.println("GIF exported successfully: " + filepath);
            System.out.println("File size: " + new File(filepath).length() + " bytes");
        } catch (Exception e) {
            System.err.println("Error saving GIF: " + e.getMessage());
            e.printStackTrace();
        }

        MetaManager.exportMetadata(portrait, getMetadataFullPath(filepath, metadataPath));
    }
    
    private String getMetadataFullPath(String filepath, String metadataPath) {
        if (".meta".equals(metadataPath)) {
            return filepath.substring(0, filepath.lastIndexOf('.')) + metadataPath;
        } else {
            return metadataPath;
        }
    }

    public void importDisassembly(String filePath){
        System.out.println("Importing disassembly ...");
        portrait = DisassemblyManager.importDisassembly(filePath);
        this.tiles = portrait.getTiles();
        graphicsManager.setTiles(portrait.getTiles());
        System.out.println("Disassembly imported.");
    }
    
    public void exportDisassembly(String filepath){
        System.out.println("Exporting disassembly ...");
        DisassemblyManager.exportDisassembly(portrait, filepath);
        System.out.println("Disassembly exported.");        
    }   

    public Portrait getPortrait() {
        return portrait;
    }

    public void setPortrait(Portrait portrait) {
        this.portrait = portrait;
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[] tiles) {
        this.tiles = tiles;
    }
}