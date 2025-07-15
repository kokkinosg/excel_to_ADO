package com.example.excel_to_ADO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Class which performs all required operations on the excel files to give out the required data 
public class excelReader {

    // Private variables 
    private String filePath; // path of the excel file
    private Workbook workbook; // Workbook object
    private FileInputStream file; //File object

    
    // Constructor. 
    // Takes the path to the excel file as an argument 
    public excelReader(String filePath){
        this.filePath = filePath;
    }




    // Helper functions

    // Helper method which opens the .xlsx file 
    private  boolean openXlsx(){
        try {
            this.file = new FileInputStream(new File(filePath));
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




}
