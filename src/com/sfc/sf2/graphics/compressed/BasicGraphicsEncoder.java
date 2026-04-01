/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.graphics.compressed;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.graphics.uncompressed.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author wiz
 */
public class BasicGraphicsEncoder {
    
    
    private static byte[] newGraphicsFileBytes;  
    
    private static final Logger LOG = Logger.getLogger(BasicGraphicsEncoder.class.getName());  
    
    public static void produceGraphics(Tile[] tiles){
        LOG.entering(LOG.getName(),"produceGraphics");
        UncompressedGraphicsEncoder.produceGraphics(tiles);
        byte[] input = UncompressedGraphicsEncoder.getNewGraphicsFileBytes();
        LOG.fine("input = " + bytesToHex(input));
        byte[] output = null;
        List<Short> outputWords = new ArrayList();
        Short currentCommandWord = null;
        int commandWordCursor = 0;
        int commandWordIndex = 0;
        int inputPointer = 0;
        Short previousWord = null;
        while(inputPointer<input.length){
            
            if(commandWordCursor % 16 == 0){
                currentCommandWord = (short)0;
                commandWordIndex = outputWords.size();
                outputWords.add((short)(currentCommandWord & 0xFFFF));
                commandWordCursor = 0;
            }
            
            ByteBuffer inputbb = ByteBuffer.allocate(2);
            inputbb.order(ByteOrder.LITTLE_ENDIAN);
            inputbb.put(input[inputPointer+1]);
            inputbb.put(input[inputPointer]);          
            Short inputWord = inputbb.getShort(0);
            
            LOG.fine("inputWord = " + Integer.toHexString(inputWord & 0xFFFF));
            
            /* Get number of potentially repeatable words */
            int potentialRepeats = 0;
            if(inputWord.equals(previousWord)){
                Short nextWord = inputWord;
                int testCursor = inputPointer;
                while(nextWord.equals(previousWord)){
                    potentialRepeats++;
                    // Push further if possible or stop
                    if(potentialRepeats==33){
                        break;
                    }                    
                    testCursor+=2;
                    if(testCursor+1<input.length){
                        ByteBuffer testbb = ByteBuffer.allocate(2);
                        testbb.order(ByteOrder.LITTLE_ENDIAN);
                        testbb.put(input[testCursor+1]);
                        testbb.put(input[testCursor]);          
                        nextWord = testbb.getShort(0);                        
                    }else{
                        break;
                    }
                } 
                LOG.fine("Potential repeats = " + potentialRepeats);
            }
        
            /* Get number of potential word sequence to copy */
            int potentialCopyLength = 0;
            int candidateSourceCursor = 0;
            int sourceCursor = inputPointer-4;
            while(sourceCursor>=0){
                int testLength = 0;
                Short destWord = inputWord;
                ByteBuffer sourcebb = ByteBuffer.allocate(2);
                sourcebb.order(ByteOrder.LITTLE_ENDIAN);
                sourcebb.put(input[sourceCursor+1]);
                sourcebb.put(input[sourceCursor]);          
                Short sourceWord = sourcebb.getShort(0);
                while(sourceWord.equals(destWord)){
                    testLength++;
                    // Push further if possible or stop
                    if(testLength==33){
                        break;
                    }
                    if((inputPointer+testLength*2)<input.length){
                        sourcebb = ByteBuffer.allocate(2);
                        sourcebb.order(ByteOrder.LITTLE_ENDIAN);
                        sourcebb.put(input[sourceCursor+testLength*2+1]);
                        sourcebb.put(input[sourceCursor+testLength*2]);          
                        sourceWord = sourcebb.getShort(0);                        
                        ByteBuffer destbb = ByteBuffer.allocate(2);
                        destbb.order(ByteOrder.LITTLE_ENDIAN);
                        destbb.put(input[inputPointer+testLength*2+1]);
                        destbb.put(input[inputPointer+testLength*2]);          
                        destWord = destbb.getShort(0);
                    }else{
                        break;
                    }
                } 
                if(testLength>potentialCopyLength){
                    candidateSourceCursor = sourceCursor;
                    potentialCopyLength = testLength;
                }
                sourceCursor-=2;      
            }
            LOG.fine("Potential copy length from " + candidateSourceCursor + " = " + potentialCopyLength); 
            
            if(potentialRepeats>1 || potentialCopyLength>1){
                if(potentialRepeats>=potentialCopyLength){
                    // Apply word repeat
                    int repeatValue = 33 - potentialRepeats;
                    short repeatWord = (short) (0x0020 | repeatValue);
                    outputWords.add(repeatWord);
                    LOG.fine("repeatWord = " + Integer.toHexString(repeatWord & 0xFFFF));
                    inputPointer+=2*potentialRepeats;
                }else{
                    // Apply word sequence copy
                    int startOffset = (inputPointer - candidateSourceCursor) / 2;
                    int sequenceLength = 33 - potentialCopyLength;
                    short repeatWord = (short) ((short)(startOffset<<5) | sequenceLength);
                    outputWords.add(repeatWord);
                    LOG.fine("repeatWord = " + Integer.toHexString(repeatWord & 0xFFFF));
                    inputPointer+=2*potentialCopyLength;
                }
                outputWords.set(commandWordIndex, (short) (outputWords.get(commandWordIndex) | (0x8000 >> commandWordCursor)));
            }else{
                // No repeat or copy : just output the word
                outputWords.add(inputWord);
                inputPointer+=2;
            }
            ByteBuffer previousbb = ByteBuffer.allocate(2);
            previousbb.order(ByteOrder.LITTLE_ENDIAN);
            previousbb.put(input[inputPointer-2+1]);
            previousbb.put(input[inputPointer-2]);          
            previousWord = previousbb.getShort(0);            
            commandWordCursor++;
            LOG.fine("output = " + shortListToHex(outputWords));
        }
        if(commandWordCursor % 16 == 0){
            currentCommandWord = (short)0;
            commandWordIndex = outputWords.size();
            outputWords.add(currentCommandWord);
            commandWordCursor = 0;
        }  
        outputWords.set(commandWordIndex, (short) (outputWords.get(commandWordIndex) | (0x8000 >> commandWordCursor)));
        outputWords.add((short)0);
        LOG.fine("output = " + shortListToHex(outputWords));
        
        output = new byte[outputWords.size()*2];
        for(int i=0;i<outputWords.size();i++){
            short word = outputWords.get(i);
            output[i*2] = (byte)((word >> 8) & 0xff);
            output[i*2+1] = (byte)(word & 0xff);
        }
        LOG.fine("output bytes length = " + output.length);
        LOG.exiting(LOG.getName(),"produceGraphics");
        newGraphicsFileBytes = output;
    }
    
    public static byte[] getNewGraphicsFileBytes(){
        return newGraphicsFileBytes;
    }
    
    final protected static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }    
    public static String byteListToHex(List<Byte> bytes) {
        char[] hexChars = new char[bytes.size() * 2];
        for ( int j = 0; j < bytes.size(); j++ ) {
            int v = bytes.get(j) & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }   
    public static String shortListToHex(List<Short> shorts) {
        char[] hexChars = new char[shorts.size() * 4];
        for ( int j = 0; j < shorts.size(); j++ ) {
            short v = (short)(shorts.get(j) & 0xFFFF);
            hexChars[j * 4] = HEX_ARRAY[(v & 0xF000) >>> 12];
            hexChars[(j * 4) + 1] = HEX_ARRAY[(v & 0x0F00) >>> 8];
            hexChars[(j * 4) + 2] = HEX_ARRAY[(v & 0x00F0) >>> 4];
            hexChars[(j * 4) + 3] = HEX_ARRAY[(v & 0x000F)];            
        }
        return new String(hexChars);
    }      

}
