/*
 * DisassemblyManager.java - 64 COLOR FINAL (force indexedColorImage + row-major)
 */
package com.sfc.sf2.portrait.io;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.palette.Palette64;
import com.sfc.sf2.portrait.Portrait;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DisassemblyManager {

    public static Portrait importDisassembly(String filepath) {
        System.out.println("=== Portrait importDisassembly 64-color FORCE IMAGE ===");
        Portrait portrait = new Portrait();
        try {
            byte[] data = Files.readAllBytes(Paths.get(filepath));

            short eyesNum = getNextWord(data, 0);
            int offset = 2 + eyesNum * 4;
            short mouthNum = getNextWord(data, offset);
            offset += 2 + mouthNum * 4;

            portrait.setEyeTiles(new int[Math.min(eyesNum, 32)][4]);
            portrait.setMouthTiles(new int[Math.min(mouthNum, 32)][4]);

            // Палитра 128 байт
            Palette64 palette = null;
            if (offset + 128 <= data.length) {
                byte[] palData = new byte[128];
                System.arraycopy(data, offset, palData, 0, 128);

                Color[] colors = new Color[64];
                for (int i = 0; i < 64; i++) {
                    int word = (palData[i*2] & 0xFF) | ((palData[i*2+1] & 0xFF) << 8);
                    int r = (word >> 10) & 0x1F;
                    int g = (word >> 5) & 0x1F;
                    int b = word & 0x1F;
                    colors[i] = new Color(r << 3, g << 3, b << 3);
                }
                palette = new Palette64(colors);
                System.out.println("Loaded 64-color palette (128 bytes)");
            }

            int gfxOffset = offset + 128;
            byte[] rawData = new byte[Math.min(4096, data.length - gfxOffset)];
            System.arraycopy(data, gfxOffset, rawData, 0, rawData.length);

            Tile[] tiles = new Tile[64];
            int byteIndex = 0;

            // Создаём ICM один раз (как в PNG импорте)
            int[] paletteData = new int[64];
            if (palette != null) {
                Color[] cols = palette.getColors();
                for (int i = 0; i < 64; i++) {
                    paletteData[i] = cols[i].getRGB();
                }
            }
            IndexColorModel icm64 = new IndexColorModel(6, 64, paletteData, 0, true, 0, DataBufferByte.TYPE_BYTE);

            for (int t = 0; t < 64; t++) {
                Tile tile = new Tile();
                int[][] pixels = new int[8][8];

                // ROW-MAJOR (самый стабильный для твоего случая)
                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 8; x++) {
                        if (byteIndex < rawData.length) {
                            pixels[y][x] = rawData[byteIndex++] & 0xFF;
                        }
                    }
                }

                tile.setPixels(pixels);

                // Принудительно создаём изображение тайла (как при импорте PNG)
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

                // Палитру тоже ставим (на всякий случай)
                if (palette != null) {
                    try {
                        java.lang.reflect.Field f = Tile.class.getDeclaredField("palette");
                        f.setAccessible(true);
                        f.set(tile, palette);
                    } catch (Exception ignored) {}
                }

                tiles[t] = tile;
            }

            portrait.setTiles(tiles);
            System.out.println("Successfully loaded 64 tiles with FORCED indexedColorImage");

        } catch (Exception e) {
            System.err.println("Error while parsing disassembly: " + e.getMessage());
            e.printStackTrace();
        }
        return portrait;
    }

    public static void exportDisassembly(Portrait portrait, String filepath) {
        System.out.println("=== Portrait exportDisassembly 64-color ===");
        try {
            Tile[] tiles = portrait.getTiles();
            if (tiles == null || tiles.length == 0) return;

            Palette64 palette = null;
            if (tiles.length > 0 && tiles[0] != null) {
                try {
                    java.lang.reflect.Field f = Tile.class.getDeclaredField("palette");
                    f.setAccessible(true);
                    Object p = f.get(tiles[0]);
                    if (p instanceof Palette64) palette = (Palette64) p;
                } catch (Exception ignored) {}
            }

            byte[] paletteBytes = new byte[128];
            if (palette != null) {
                Color[] colors = palette.getColors();
                for (int i = 0; i < 64; i++) {
                    Color c = colors[i];
                    int r = (c.getRed() >> 3) & 0x1F;
                    int g = (c.getGreen() >> 3) & 0x1F;
                    int b = (c.getBlue() >> 3) & 0x1F;
                    int word = (r << 10) | (g << 5) | b;
                    paletteBytes[i*2]   = (byte)(word & 0xFF);
                    paletteBytes[i*2+1] = (byte)((word >> 8) & 0xFF);
                }
            }

            byte[] tileData = new byte[4096];
            int idx = 0;
            for (int t = 0; t < 64; t++) {
                int[][] px = tiles[t].getPixels();
                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 8; x++) {
                        tileData[idx++] = (byte) px[y][x];
                    }
                }
            }

            byte[] header = new byte[4];

            byte[] fullData = new byte[header.length + 128 + 4096];
            System.arraycopy(header, 0, fullData, 0, header.length);
            System.arraycopy(paletteBytes, 0, fullData, header.length, 128);
            System.arraycopy(tileData, 0, fullData, header.length + 128, 4096);

            Files.write(Paths.get(filepath), fullData);
            System.out.println("Successfully exported " + fullData.length + " bytes to " + filepath);

        } catch (Exception e) {
            System.err.println("Error during export: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static short getNextWord(byte[] data, int cursor) {
        if (cursor + 1 >= data.length) return 0;
        ByteBuffer bb = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
        bb.put(data[cursor + 1]);
        bb.put(data[cursor]);
        return bb.getShort(0);
    }
}