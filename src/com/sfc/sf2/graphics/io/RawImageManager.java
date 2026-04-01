/*
 * RawImageManager.java - 64 COLOR VERSION (with getImportedImageTileWidth)
 */
package com.sfc.sf2.graphics.io;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.palette.Palette;
import com.sfc.sf2.palette.Palette64;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author wiz
 */
public class RawImageManager {
   
    public static final int FILE_FORMAT_PNG = 0;
    public static final int FILE_FORMAT_GIF = 1;
   
    private static int importedImageTileWidth = 8; // для портретов 64x64 = 8 тайлов по ширине
    
    public static int getImportedImageTileWidth() {
        return importedImageTileWidth;
    }
    
    public static void setImportedImageTileWidth(int width) {
        importedImageTileWidth = width;
    }

    private static final Logger LOG = Logger.getLogger(RawImageManager.class.getName());
   
    public static Tile[] importImage(String filepath) {
        LOG.entering(LOG.getName(), "importImage");
        Tile[] tiles = null;
        try {
            Path path = Paths.get(filepath);
            BufferedImage img = ImageIO.read(path.toFile());

            int width = img.getWidth();
            int height = img.getHeight();
            if (width % 8 != 0 || height % 8 != 0) {
                LOG.warning("Image dimensions not multiple of 8");
            }

            importedImageTileWidth = width / 8;
            int totalTiles = (width / 8) * (height / 8);
            tiles = new Tile[totalTiles];

            WritableRaster raster = img.getRaster();
            int[] pixelArray = new int[64];

            for (int t = 0; t < totalTiles; t++) {
                int tileX = (t % importedImageTileWidth) * 8;
                int tileY = (t / importedImageTileWidth) * 8;

                Tile tile = new Tile();
                tile.setId(t);

                raster.getPixels(tileX, tileY, 8, 8, pixelArray);
                int[][] tilePixels = new int[8][8];
                for (int j = 0; j < 8; j++) {
                    for (int i = 0; i < 8; i++) {
                        tilePixels[j][i] = pixelArray[i + j * 8];
                    }
                }
                tile.setPixels(tilePixels);

                tiles[t] = tile;
            }

            LOG.exiting(LOG.getName(), "importImage");
        } catch (Exception e) {
            LOG.throwing(LOG.getName(), "importImage", e);
        }
        return tiles;
    }

    public static void exportImage(Tile[] tiles, String filepath, int tilesPerRow, int fileFormat) {
        try {
            LOG.entering(LOG.getName(), "exportImage");

            int imageWidth = tilesPerRow * 8;
            int imageHeight = ((tiles.length + tilesPerRow - 1) / tilesPerRow) * 8;

            Palette palette = (tiles != null && tiles.length > 0) ? tiles[0].getPalette() : null;
            IndexColorModel icm = null;
            if (palette instanceof Palette64) {
                icm = ((Palette64) palette).getIcm();
            } else if (palette != null) {
                icm = palette.getIcm();
            }

            BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_INDEXED, icm);
            WritableRaster raster = image.getRaster();

            int[] pixels = new int[64];
            for (int t = 0; t < tiles.length; t++) {
                if (tiles[t] != null) {
                    int[][] tilePx = tiles[t].getPixels();
                    for (int j = 0; j < 8; j++) {
                        for (int i = 0; i < 8; i++) {
                            pixels[i + j * 8] = tilePx[j][i];
                        }
                    }
                    int x = (t % tilesPerRow) * 8;
                    int y = (t / tilesPerRow) * 8;
                    raster.setPixels(x, y, 8, 8, pixels);
                }
            }

            exportImage(image, filepath, tilesPerRow, fileFormat);
            LOG.exiting(LOG.getName(), "exportImage");
        } catch (Exception ex) {
            LOG.throwing(LOG.getName(), "exportImage", ex);
        }
    }

    public static void exportImage(BufferedImage image, String filepath, int tilesPerRow, int fileFormat) {
        try {
            File outputfile = new File(filepath);
            String ext = (fileFormat == FILE_FORMAT_PNG) ? "png" : "gif";
            ImageIO.write(image, ext, outputfile);
            LOG.fine("Exported image to " + outputfile.getAbsolutePath());
        } catch (IOException ex) {
            LOG.throwing(LOG.getName(), "exportImage", ex);
        }
    }
}