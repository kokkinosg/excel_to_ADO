package com.example.excel_to_ADO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Class which performs all required operations on the excel files to give out the required data 
public class ExcelReader {

    // Private variables 
    private Workbook workbook; // Workbook object
    private Sheet sheet;
    private List<RowData> sheetData; // List of row data which is essentially all data in a sheet without the header. 
    

    // Constructor. 
    // Takes the path to the excel file as an argument and the sheet number with the data and will open the xlsx, workbook and then the sheet. 
    public ExcelReader(String filePath, String sheetName){

        if (openXlsx(filePath) && openSheet(sheetName)){
            retrieveRowData();
        }
    }

    // The getter for the sheet data. 
    public List<RowData> getSheetData(){
        return sheetData;
    }

    // Helper functions
    // Retrieve all data for each row and add them to the list of row data.
    private void retrieveRowData(){
        // Instantiate an empty rows list
        this.sheetData = new ArrayList<>();
        // We want to ignore the header row
        boolean skipHeader = true;

        // Go over each row in the sheet.
        for (Row r : sheet) {

            // The header row is always the first one. So if we decided to ignore it, we simply continue to the net iteration.
            if (skipHeader) { 
                skipHeader = false; 
                continue;
            }

            // Add a new record everytime to the sheetData List
            this.sheetData.add(new RowData(
                    getInt (r, 0),   // Parent ADO ID - A
                    getStr (r, 1),   // Parent Title - B
                    getInt (r, 2),   // Child ADO ID - C
                    getStr (r, 3),   // Child Work-item Type - D
                    getStr (r, 4),   // Work-item Title - E
                    getStr (r, 5))); // Acceptance criteria - F
        }
    }

    // When the column is only Integers (ADO IDs). I am returning an Integer so that I can also deal with nulls
    private Integer getInt(Row r, int column){
        // Get the cell at at specified column number and if it is blank, return null.
        Cell c = r.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        // if the cell is blank return null
        if(c == null){
            return null;
        }

        // Deal with different cases
        switch (c.getCellType()){
            // if the cell type is numeric, return the int
            case NUMERIC:
                return (int) c.getNumericCellValue();
            // if the cell type is string, then see if it is blank and if not convert it to an integer 
            case STRING:
                if (c.getStringCellValue().isBlank()){
                    return null;
                } else{
                    return Integer.valueOf(c.getStringCellValue());
                }
            default:
                return null;
        }
    }

    // When the column is String i am either returning an empty string or the actual string
    private String getStr(Row r, int column){
        // Get the cell at at specified column number and if it is blank, return null.
        Cell c = r.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        // if the cell is blank return null
        if(c == null){
            return "";
        }
        // If it is not null, return the string. 
        return c.toString();

    }

    // Helper method which opens the workbook and leaves only the Workbook open.
    // The FileInputStream is closed immediately after loading.
    private boolean openXlsx(String filePath){
        try (FileInputStream file = new FileInputStream(filePath)) { // This way the stream auto closes. 
            this.workbook = new XSSFWorkbook(file); 
            return true;
        } catch (FileNotFoundException e) { //Catch the file not found exception which can be thrown at FileInputStream
            System.out.println("File not found: " + e.getMessage());
            return false;
        } catch (IOException e) { // Catch IOException from XSSFWorkbooks
            System.out.println("IO Exception: " + e.getMessage());
            return false;
        }
    }

    // Method to open a sheet from a specified workbook. 
    private boolean openSheet(String sheetName){
        try {
            this.sheet = workbook.getSheet(sheetName);
            return true;
        } catch (Exception e) {
            System.out.println("Error when opening the sheet: " + e.getMessage());
            return false;
        }
    }

    
}
