package com.example.excel_to_ADO;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        // Instantiate the client class
        AdoRESTClient client = new AdoRESTClient();
        // Path to the excel sheet
        String filePath = "/Users/george/Library/CloudStorage/OneDrive-SphereBioLimited/Work/Compensation/Compensation_Assay_Requirements.xlsx";
        String sheetName = "Dispensing";
        

        // Check whether the connection is succesfull 
        if(client.testConnection()){

            // Carry out the data retrieval from the excel sheet
            ExcelReader excelReader = new ExcelReader(filePath, sheetName);
            // Extract the sheetData to a local list
            List<RowData> sheetData = excelReader.getSheetData();

            // Add each row to dev ops
            for(RowData row : sheetData){
                // "Hierarchy-Reverse" is when each row is a child to the parent. 
                client.createWorkItem(row.childType(), row.childTitle(), row.acceptanceCriteria(), row.parentId(), "Hierarchy-Reverse");
            }
        }
    }
}
