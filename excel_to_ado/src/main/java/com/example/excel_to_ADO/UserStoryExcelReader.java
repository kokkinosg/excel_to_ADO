package com.example.excel_to_ADO;

import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Row;

import com.example.excel_to_ADO.Data_Structures.UserStoryData;

public class UserStoryExcelReader {

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
            this.sheetData.add(new UserStoryData(
                    getInt (r, 0),   // Parent ADO ID - A
                    getStr (r, 1),   // Parent Title - B
                    getInt (r, 2),   // Child ADO ID - C
                    getStr (r, 3),   // Child Work-item Type - D
                    getStr (r, 4),   // Work-item Title - E
                    getStr (r, 5))); // Acceptance criteria - F
        }
    }
    
}
