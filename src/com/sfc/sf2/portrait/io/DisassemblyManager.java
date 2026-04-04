/*
 * DisassemblyManager.java - FIXED (blink/talk header + palette + graphics)
 */
package com.sfc.sf2.portrait.io;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.palette.Palette64;
import com.sfc.sf2.portrait.Portrait;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;

public class DisassemblyManager {

    public static Portrait importDisassembly(String filepath) {
        System.out.println("=== Portrait importDisassembly (blink/talk + palette + 6BPP) ===");
        Portrait portrait = new Portrait();
        try {
            byte[] data = Files.readAllBytes(Paths.get(filepath));
            int offset = 0;

            // ====================== BLINK/TALK HEADER ======================
            // eyes count (2 bytes, little-endian)
            int eyesCount = (data[offset++] & 0xFF) | ((data[offset++] & 0xFF) << 8);
            int[][] eyeTiles = new int[eyesCount][4];
            for (int i = 0; i < eyesCount; i++) {
                for (int j = 0; j < 4; j++) {
                    eyeTiles[i][j] = data[offset++] & 0xFF;
                }
            }

            // mouths count (2 bytes)
            int mouthCount = (data[offset++] & 0xFF) | ((data[offset++] & 0xFF) << 8);
            int[][] mouthTiles = new int[mouthCount][4];
            for (int i = 0; i < mouthCount; i++) {
                for (int j = 0; j < 4; j++) {
                    mouthTiles[i][j] = data[offset++] & 0xFF;
                }
            }

            portrait.setEyeTiles(eyeTiles);
            portrait.setMouthTiles(mouthTiles);
            System.out.println("Loaded blink/talk: " + eyesCount + " eyes, " + mouthCount + " mouths");

            // ====================== ПАЛИТРА 128 байт ======================
            byte[] palData = new byte[128];
            System.arraycopy(data, offset, palData, 0, 128);
            offset += 128;

            Color[] colors = new Color[64];
            for (int i = 0; i < 64; i++) {
                int word = (palData[i*2] & 0xFF) | ((palData[i*2 + 1] & 0xFF) << 8);
                int r = (word >> 10) & 0x1F;
                int g = (word >> 5) & 0x1F;
                int b = word & 0x1F;
                colors[i] = new Color(r << 3, g << 3, b << 3);
            }
            Palette64 palette = new Palette64(colors);

            // ====================== ГРАФИКА (3072 байта) ======================
            Tile[] tiles = new Tile[64];
            int byteIndex = offset;

            int[] paletteData = new int[64];
            Color[] cols = palette.getColors();
            for (int i = 0; i < 64; i++) paletteData[i] = cols[i].getRGB();

            IndexColorModel icm64 = new IndexColorModel(6, 64, paletteData, 0, true, 0, DataBufferByte.TYPE_BYTE);

            for (int t = 0; t < 64; t++) {
                Tile tile = new Tile();
                int[][] pixels = new int[8][8];

                for (int y = 0; y < 8; y++) {
                    byte bp0 = data[byteIndex++]; // bit 0
                    byte bp1 = data[byteIndex++]; // bit 1
                    byte bp2 = data[byteIndex++]; // bit 2
                    byte bp3 = data[byteIndex++]; // bit 3
                    byte bp5 = data[byteIndex++]; // bit 5
                    byte bp4 = data[byteIndex++]; // bit 4

                    for (int x = 0; x < 8; x++) {
                        int color = 0;
                        if ((bp0 & (0x80 >> x)) != 0) color |= 0x01;
                        if ((bp1 & (0x80 >> x)) != 0) color |= 0x02;
                        if ((bp2 & (0x80 >> x)) != 0) color |= 0x04;
                        if ((bp3 & (0x80 >> x)) != 0) color |= 0x08;
                        if ((bp4 & (0x80 >> x)) != 0) color |= 0x10;
                        if ((bp5 & (0x80 >> x)) != 0) color |= 0x20;
                        pixels[y][x] = color;
                    }
                }
                tile.setPixels(pixels);

                // Создание изображения тайла (reflection)
                BufferedImage tileImage = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_INDEXED, icm64);
                byte[] imgData = ((DataBufferByte) tileImage.getRaster().getDataBuffer()).getData();
                for (int py = 0; py < 8; py++) {
                    for (int px = 0; px < 8; px++) {
                        imgData[py * 8 + px] = (byte) pixels[py][px];
                    }
                }
                try {
                    java.lang.reflect.Field field = Tile.class.getDeclaredField("indexedColorImage");
                    field.setAccessible(true);
                    field.set(tile, tileImage);
                } catch (Exception ignored) {}

                try {
                    java.lang.reflect.Field f = Tile.class.getDeclaredField("palette");
                    f.setAccessible(true);
                    f.set(tile, palette);
                } catch (Exception ignored) {}

                tiles[t] = tile;
            }

            portrait.setTiles(tiles);

            // Попытка загрузить .meta (если существует)
            String metaPath = filepath + ".meta";
            if (new File(metaPath).exists()) {
                MetaManager.importMetadata(portrait, metaPath);
            }

            System.out.println("✅ Successfully imported portrait from .bin");

        } catch (Exception e) {
            System.err.println("Error while parsing disassembly: " + e.getMessage());
            e.printStackTrace();
        }
        return portrait;
    }

    // exportDisassembly оставляем почти как был, но исправляем цикл для глаз
    public static void exportDisassembly(Portrait portrait, String filepath) {
        System.out.println("=== Portrait exportDisassembly (blink/talk + .meta) ===");

        try {
            Tile[] tiles = portrait.getTiles();
            if (tiles == null || tiles.length != 64) {
                System.err.println("Error: Portrait must have exactly 64 tiles");
                return;
            }

            int[][] eyeTiles = portrait.getEyeTiles() != null ? portrait.getEyeTiles() : new int[0][4];
            int[][] mouthTiles = portrait.getMouthTiles() != null ? portrait.getMouthTiles() : new int[0][4];

            int headerSize = 2 + (eyeTiles.length * 4) + 2 + (mouthTiles.length * 4);
            byte[] data = new byte[headerSize + 128 + 3072];

            int offset = 0;

            // Blink/Talk header
            data[offset++] = (byte) (eyeTiles.length & 0xFF);
            data[offset++] = (byte) ((eyeTiles.length >> 8) & 0xFF);
            for (int i = 0; i < eyeTiles.length; i++) {
                for (int j = 0; j < 4; j++) {
                    data[offset++] = (byte) (eyeTiles[i][j] & 0xFF);
                }
            }
            data[offset++] = (byte) (mouthTiles.length & 0xFF);
            data[offset++] = (byte) ((mouthTiles.length >> 8) & 0xFF);
            for (int i = 0; i < mouthTiles.length; i++) {
                for (int j = 0; j < 4; j++) {
                    data[offset++] = (byte) (mouthTiles[i][j] & 0xFF);
                }
            }

            // Palette 128 bytes
            Palette64 palette = (tiles[0].getPalette() instanceof Palette64) ? 
                                (Palette64) tiles[0].getPalette() : null;
            Color[] colors = palette != null ? palette.getColors() : new Color[64];
            for (int i = 0; i < 64; i++) {
                Color c = (i < colors.length && colors[i] != null) ? colors[i] : Color.BLACK;
                int r = (c.getRed() >> 3) & 0x1F;
                int g = (c.getGreen() >> 3) & 0x1F;
                int b = (c.getBlue() >> 3) & 0x1F;
                int word = (r << 10) | (g << 5) | b;
                data[offset++] = (byte) (word & 0xFF);
                data[offset++] = (byte) ((word >> 8) & 0xFF);
            }

            // Graphics 3072 bytes
            int byteIndex = offset;
            for (int t = 0; t < 64; t++) {
                int[][] pixels = tiles[t].getPixels();
                for (int y = 0; y < 8; y++) {
                    byte bp0 = 0, bp1 = 0, bp2 = 0, bp3 = 0, bp4 = 0, bp5 = 0;
                    for (int x = 0; x < 8; x++) {
                        int color = pixels[y][x] & 0x3F;
                        if ((color & 0x01) != 0) bp0 |= (byte)(0x80 >> x);
                        if ((color & 0x02) != 0) bp1 |= (byte)(0x80 >> x);
                        if ((color & 0x04) != 0) bp2 |= (byte)(0x80 >> x);
                        if ((color & 0x08) != 0) bp3 |= (byte)(0x80 >> x);
                        if ((color & 0x10) != 0) bp4 |= (byte)(0x80 >> x);
                        if ((color & 0x20) != 0) bp5 |= (byte)(0x80 >> x);
                    }
                    data[byteIndex++] = bp0;
                    data[byteIndex++] = bp1;
                    data[byteIndex++] = bp2;
                    data[byteIndex++] = bp3;
                    data[byteIndex++] = bp5;
                    data[byteIndex++] = bp4;
                }
            }

            Files.write(Paths.get(filepath), data);
            System.out.println("✅ Exported .bin (" + data.length + " bytes)");

            // Meta
            String metaPath = filepath + ".meta";
            MetaManager.exportMetadata(portrait, metaPath);

        } catch (Exception e) {
            System.err.println("Error while exporting: " + e.getMessage());
            e.printStackTrace();
        }
    }
}