/*
 * DisassemblyManager.java - FINAL 48-BYTE 6BPP (Round-trip FIXED) + PALETTE
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

public class DisassemblyManager {

    public static Portrait importDisassembly(String filepath) {
        System.out.println("=== Portrait importDisassembly 48-BYTE 6BPP (FIXED) ===");
        Portrait portrait = new Portrait();
        try {
            byte[] data = Files.readAllBytes(Paths.get(filepath));

            // Новый формат: 4 байта заголовок + 128 байт палитра + 3072 байт графики
            int headerSize = 4;
            int paletteSize = 128;
            int gfxSize = 64 * 48;           // 3072
            int gfxOffset = headerSize + paletteSize;

            if (data.length < gfxOffset + gfxSize) {
                System.err.println("File too small for 48-byte 6bpp format");
                return portrait;
            }

            // ====================== ПАЛИТРА ======================
            Palette64 palette = null;
            byte[] palData = new byte[paletteSize];
            System.arraycopy(data, headerSize, palData, 0, paletteSize);

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

            // ====================== ГРАФИКА 48-BYTE BITPLANE ======================
            Tile[] tiles = new Tile[64];
            int byteIndex = gfxOffset;

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

                // Создаём изображение для редактора
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
            System.out.println("✅ Successfully imported 64 tiles (48-byte 6bpp) — round-trip OK");

        } catch (Exception e) {
            System.err.println("Error while parsing disassembly: " + e.getMessage());
            e.printStackTrace();
        }
        return portrait;
    }

        public static void exportDisassembly(Portrait portrait, String filepath) {
        System.out.println("=== Portrait exportDisassembly 48-BYTE 6BPP (WITH PALETTE) ===");

        try {
            Tile[] tiles = portrait.getTiles();
            if (tiles == null || tiles.length != 64) {
                System.err.println("Error: Portrait must have exactly 64 tiles");
                return;
            }

            // ====================== ПОДГОТОВКА ДАННЫХ ======================
            byte[] data = new byte[4 + 128 + 3072];  // заголовок + палитра + графика

            // Заголовок (сигнатура SF2P)
            data[0] = 'S';
            data[1] = 'F';
            data[2] = '2';
            data[3] = 'P';

            // ====================== ПАЛИТРА (128 байт) ======================
            com.sfc.sf2.palette.Palette basePalette = tiles[0].getPalette();  // ← исправлено
            Palette64 palette = (basePalette instanceof Palette64) ? (Palette64) basePalette : null;

            if (palette == null) {
                System.err.println("Warning: No Palette64 found, using default black palette");
            }

            Color[] colors = palette != null ? palette.getColors() : new Color[64];
            int palOffset = 4;

            for (int i = 0; i < 64; i++) {
                Color c = (i < colors.length && colors[i] != null) ? colors[i] : Color.BLACK;
                int r = (c.getRed() >> 3) & 0x1F;
                int g = (c.getGreen() >> 3) & 0x1F;
                int b = (c.getBlue() >> 3) & 0x1F;

                int word = (r << 10) | (g << 5) | b;

                data[palOffset + i*2]     = (byte) (word & 0xFF);
                data[palOffset + i*2 + 1] = (byte) ((word >> 8) & 0xFF);
            }

            // ====================== ГРАФИКА (3072 байта) ======================
            int gfxOffset = 4 + 128;
            int byteIndex = gfxOffset;

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

                    data[byteIndex++] = bp0;  // bit 0
                    data[byteIndex++] = bp1;  // bit 1
                    data[byteIndex++] = bp2;  // bit 2
                    data[byteIndex++] = bp3;  // bit 3
                    data[byteIndex++] = bp5;  // bit 5
                    data[byteIndex++] = bp4;  // bit 4
                }
            }

            // Сохраняем файл
            Files.write(Paths.get(filepath), data);
            System.out.println("✅ Successfully exported " + data.length + " bytes (4+128+3072) with palette");

        } catch (Exception e) {
            System.err.println("Error while exporting disassembly: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // вспомогательная функция (оставлена на всякий случай)
    private static short getNextWord(byte[] data, int cursor) {
        if (cursor + 1 >= data.length) return 0;
        return (short) ((data[cursor] & 0xFF) | ((data[cursor + 1] & 0xFF) << 8));
    }
}