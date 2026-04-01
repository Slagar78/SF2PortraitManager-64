/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.portrait.gui;

import com.sfc.sf2.portrait.layout.PortraitLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author wiz
 */
public class PortraitTableModel extends AbstractTableModel {
    
    private int[][] tableData;
    private final String[] columns = {"X", "Y", "X'", "Y'"};
    
    private PortraitLayout layout;
 
    public PortraitTableModel(int[][] altTiles, PortraitLayout portraitLayout) {
        super();
        layout = portraitLayout;
        tableData = new int[altTiles!=null?altTiles.length:0][];
        int i = 0;
        if(altTiles!=null){
            while(i<altTiles.length){
                tableData[i] = new int[4];
                tableData[i][0] = altTiles[i][0];
                tableData[i][1] = altTiles[i][1];
                tableData[i][2] = altTiles[i][2];
                tableData[i][3] = altTiles[i][3];
                i++;
            }
        }
    }
    
    @Override
    public Object getValueAt(int row, int col) {
        return tableData[row][col];
    }
    @Override
    public void setValueAt(Object value, int row, int col) {
        tableData[row][col] = Integer.parseInt((String)value);
    }    
 
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }    
    
    @Override
    public int getRowCount() {
        return tableData.length;
    }
 
    @Override
    public int getColumnCount() {
        return columns.length;
    }
 
    @Override
    public String getColumnName(int columnIndex) {
        return columns[columnIndex];
    }
 
    public int[][] produceTiles(){
        List<int[]> entries = new ArrayList<>();
        for(int[] entry : tableData){
            try{
                int[] intEntry = new int[4];
                intEntry[0] = entry[0];
                intEntry[1] = entry[1];
                intEntry[2] = entry[2];
                intEntry[3] = entry[3];
                entries.add(intEntry);
            }catch(Exception e){
                break;
            }
        }
        int[][] returnedEntries = new int[entries.size()][4];
        return entries.toArray(returnedEntries);
    }
    
    public void addRow(){
        if(tableData.length<12){
            int[][] newTable = new int[tableData.length+1][];
            for(int i=0;i<tableData.length;i++){
                newTable[i] = tableData[i];
            }
            newTable[newTable.length-1] = new int[4];
            newTable[newTable.length-1][0] = 0;
            newTable[newTable.length-1][1] = 0;
            newTable[newTable.length-1][2] = 6;
            newTable[newTable.length-1][3] = 0;
            tableData = newTable;
        }
    }
    
    public void removeRow(){
        if(tableData.length>1){
            int[][] newTable = new int[tableData.length-1][];
            for(int i=0;i<newTable.length;i++){
                newTable[i] = tableData[i];
            }
            tableData = newTable;
        }
    }

    public int[][] getTableData() {
        return tableData;
    }

    public void setTableData(int[][] tableData) {
        this.tableData = tableData;
    }
    
    
    
}
