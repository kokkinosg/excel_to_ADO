package com.example.excel_to_ADO;

import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Row;

import com.example.excel_to_ADO.Abstract_classes.ExcelReader;
import com.example.excel_to_ADO.Data_Structures.FMEARiskData;

public class RiskExcelReader extends ExcelReader<FMEARiskData> {

    // Constructor 
    public RiskExcelReader(String filePath, String sheetName){
        super(filePath,sheetName);
    }

    // Retrieve all data for each row and add them to the list of row data.
    @Override
    protected void retrieveRowData(){
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
            this.sheetData.add(
                new FMEARiskData(
                    getInt(r, 2), // Predeccessor ID - C 
                    getStr(r, 4), // Predeccessor Title - E
                    getStr(r, 8),   // Potential failure mode - I
                    getStr(r, 9),   // Potential Failure Effects - J
                    getStr(r, 10),   // Probable Cause - K
                    getStr(r, 11),   // pre Severity - L
                    getStr(r, 13),  // pre Occurance - N
                    getStr(r, 15),   // pre Detection - P
                    getStr(r, 19),   // Mitigation Type  - T
                    getStr(r, 21),  // Evidence - V
                    getStr(r, 23),   // post Severity - X
                    getStr(r, 25),  // post Occurance - Z
                    getStr(r, 27)));   // post Detection - AB        
        }
    }
    
}
