package com.example.excel_to_ADO.Abstract_classes;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Abstract, generic class which performs all required operations on the excel files to give out the required data 
public abstract class ExcelReader <T> {

    // Private variables 
    protected Workbook workbook; // Workbook object
    protected Sheet sheet;
    protected List<T> sheetData; // List of row data which is essentially all system req data in a sheet without the header. 
    

    // Constructor. 
    // Takes the path to the excel file as an argument and the sheet number with the data and will open the xlsx, workbook and then the sheet. 
    public ExcelReader(String filePath, String sheetName){

        if (openXlsx(filePath) && openSheet(sheetName)){
            retrieveRowData();
        }
    }

    // The getter for the sheet data. 
    public List<T> getSheetData(){
        return sheetData;
    }

    
    // Method to be overriden by the subclasses
    protected abstract void retrieveRowData();

    //#region Helper Functions

    // When the column is only Integers (ADO IDs). I am returning an Integer so that I can also deal with nulls
    protected Integer getInt(Row r, int column){
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
    protected String getStr(Row r, int column){
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
    protected boolean openXlsx(String filePath){
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
    protected boolean openSheet(String sheetName){
        try {
            this.sheet = workbook.getSheet(sheetName);
            return true;
        } catch (Exception e) {
            System.out.println("Error when opening the sheet: " + e.getMessage());
            return false;
        }
    }

    //#endregion
    
}
