/*
 * DisassemblyManager.java - 64 COLOR (safe offset + column-major match to Tile)
 */
package com.sfc.sf2.portrait.io;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.palette.Palette64;  // или Palette, если используешь свой
import com.sfc.sf2.portrait.Portrait;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DisassemblyManager {

    public static Portrait importDisassembly(String filepath) {
        System.out.println("=== Portrait importDisassembly (64-color safe) ===");
        Portrait portrait = new Portrait();
        try {
            byte[] data = Files.readAllBytes(Paths.get(filepath));

            // === 1. Eye and Mouth (защита от неправильного чтения) ===
            short eyesNum = getNextWord(data, 0);
            int eyesOffset = 2 + eyesNum * 4;
            int[][] eyesTiles = new int[Math.min(eyesNum, 32)][4]; // защита
            // заполнение eyesTiles ... (оставь как было в твоей версии или упрости до new int[0][4] если не используешь)

            short mouthNum = getNextWord(data, eyesOffset);
            int mouthOffset = eyesOffset + 2 + mouthNum * 4;
            int[][] mouthTiles = new int[Math.min(mouthNum, 32)][4];

            portrait.setEyeTiles(eyesTiles);
            portrait.setMouthTiles(mouthTiles);

            // === 2. Палитра 128 байт ===
            int palOffset = mouthOffset;  // после mouth
            Palette64 palette = null;
            if (palOffset + 128 <= data.length) {
                byte[] palData = new byte[128];
                System.arraycopy(data, palOffset, palData, 0, 128);

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

            // === 3. Графика — строго 4096 байт после палитры ===
            int gfxOffset = palOffset + 128;
            if (gfxOffset >= data.length) {
                System.err.println("No graphics data after palette!");
                return portrait;
            }
            byte[] rawData = new byte[Math.min(4096, data.length - gfxOffset)];
            System.arraycopy(data, gfxOffset, rawData, 0, rawData.length);

            Tile[] tiles = new Tile[64];
            int byteIndex = 0;

            for (int t = 0; t < 64; t++) {
                Tile tile = new Tile();
                int[][] pixels = new int[8][8];

                // Column-major — как в большинстве твоих Tile.getIndexedColorImage()
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        if (byteIndex < rawData.length) {
                            pixels[y][x] = rawData[byteIndex++] & 0xFF;
                        } else {
                            pixels[y][x] = 0; // fallback прозрачный
                        }
                    }
                }

                tile.setPixels(pixels);
                if (palette != null) {
                    palette.applyToTile(tile);  // или tile.setPalette(palette)
                }
                tiles[t] = tile;
            }

            portrait.setTiles(tiles);
            System.out.println("Successfully loaded 64 tiles with 64 colors (used " + byteIndex + " bytes of graphics)");

        } catch (Exception e) {
            System.err.println("Error while parsing disassembly: " + e.getMessage());
            e.printStackTrace();
        }
        return portrait;
    }

    // exportDisassembly — оставь свой последний рабочий вариант (тот, где tileData заполняется тем же column-major порядком)
    // если нужно — пришли свой текущий export, я подправлю под импорт выше

    private static short getNextWord(byte[] data, int cursor) {
        if (cursor + 1 >= data.length) return 0;
        ByteBuffer bb = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
        bb.put(data[cursor + 1]);
        bb.put(data[cursor]);
        return bb.getShort(0);
    }
}