/*
 * MetaManager.java - из оригинальной программы
 */
package com.sfc.sf2.portrait.io;

import com.sfc.sf2.portrait.Portrait;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetaManager {
    
    public static void importMetadata(Portrait portrait, String metadataPath) {
        try {
            System.out.println("=== Meta importMetadata ===");
            loadMetadataFile(portrait, metadataPath);
            System.out.println("✅ Meta file imported.");
        } catch (Exception e) {
            System.err.println("Error while parsing metadata: " + e + ". Will load without.");
        }
    }
    
    private static void loadMetadataFile(Portrait portrait, String filepath) throws IOException {
        File inputfile = new File(filepath);
        System.out.println("Loading meta: " + inputfile.getAbsolutePath());
        try (BufferedReader reader = new BufferedReader(new FileReader(inputfile))) {
            String data = reader.readLine();
            int eyesCount = Integer.parseInt(data.split(":")[1].trim());
            int[][] eyeTiles = new int[eyesCount][4];
            for (int i = 0; i < eyesCount; i++) {
                data = reader.readLine();
                String[] eyeData = data.split(",");
                for (int d = 0; d < 4; d++) {
                    eyeTiles[i][d] = Integer.parseInt(eyeData[d].trim());
                }
            }
            data = reader.readLine();
            int mouthsCount = Integer.parseInt(data.split(" ")[1].trim());
            int[][] mouthTiles = new int[mouthsCount][4];
            for (int i = 0; i < mouthsCount; i++) {
                data = reader.readLine();
                String[] mouthData = data.split(",");
                for (int d = 0; d < 4; d++) {
                    mouthTiles[i][d] = Integer.parseInt(mouthData[d].trim());
                }
            }
            portrait.setEyeTiles(eyeTiles);
            portrait.setMouthTiles(mouthTiles);
            System.out.println("Meta file imported: " + inputfile.getAbsolutePath());
        }
    }
    
    public static void exportMetadata(Portrait portrait, String metadataPath) {
        try {
            System.out.println("=== Meta exportMetadata ===");
            writeMetadataFile(portrait.getEyeTiles(), portrait.getMouthTiles(), metadataPath);
            System.out.println("✅ Meta file exported.");
        } catch (Exception ex) {
            Logger.getLogger(MetaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void writeMetadataFile(int[][] eyeTiles, int[][] mouthTiles, String filepath) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Eyes: %s\n", eyeTiles.length));
            for (int i = 0; i < eyeTiles.length; i++) {
                sb.append(String.format("%d, %d, %d, %d\n", eyeTiles[i][0], eyeTiles[i][1], eyeTiles[i][2], eyeTiles[i][3]));
            }
            sb.append(String.format("Mouths: %s\n", mouthTiles.length));
            for (int i = 0; i < mouthTiles.length; i++) {
                sb.append(String.format("%d, %d, %d, %d\n", mouthTiles[i][0], mouthTiles[i][1], mouthTiles[i][2], mouthTiles[i][3]));
            }
            
            File outputfile = new File(filepath);
            try (FileWriter writer = new FileWriter(outputfile, false)) {
                writer.write(sb.toString());
            }
            System.out.println("Meta file exported: " + outputfile.getAbsolutePath());
        } catch (Exception ex) {
            Logger.getLogger(MetaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}