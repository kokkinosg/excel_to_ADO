package com.example.excel_to_ADO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Class which performs all required operations on the excel files to give out the required data 
public class excelReader {

    // Private variables 
    private String filePath; // path of the excel file
    private Workbook workbook; // Workbook object
    private Sheet sheet;
    
    
    // Constructor. 
    // Takes the path to the excel file as an argument and the sheet number with the data and will open the xlsx, workbook and then the sheet. 
    public excelReader(String filePath, String sheetName){


        if (openXlsx(filePath) && openSheet(sheetName)){
            // Code to execute when hte sheet is obtianed
        }
    }

    


    // Helper functions

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
