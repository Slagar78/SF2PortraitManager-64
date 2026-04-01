/*
 * UncompressedGraphicsDecoder.java - 64 COLOR VERSION (6bpp raw)
 */
package com.sfc.sf2.graphics.uncompressed;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.palette.Palette;
import java.util.logging.Logger;

/**
 *
 * @author wiz
 */
public class UncompressedGraphicsDecoder {
    
    private static final Logger LOG = Logger.getLogger(UncompressedGraphicsDecoder.class.getName());
   
    public static Tile[] decodeUncompressedGraphics(byte[] data, Palette palette) {
        LOG.entering(LOG.getName(), "decodeUncompressedGraphics");
        LOG.fine("Data length = " + data.length + ", expecting " + (data.length / 64) + " tiles.");

        int tileCount = data.length / 64;
        Tile[] tiles = new Tile[tileCount];

        for (int i = 0; i < tileCount; i++) {
            Tile tile = new Tile();
            tile.setId(i);
            tile.setPalette(palette);

            int offset = i * 64;
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    // Каждый пиксель — 1 байт (0-63)
                    int pixel = data[offset + y*8 + x] & 0xFF;
                    tile.setPixel(x, y, pixel);   // предполагаем, что в Tile есть setPixel
                }
            }
            tiles[i] = tile;
        }

        LOG.exiting(LOG.getName(), "decodeUncompressedGraphics");
        return tiles;
    }
}
