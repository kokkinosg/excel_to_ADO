package com.example.excel_to_ADO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.example.excel_to_ADO.Data_Structures.FMEARiskData;
import com.example.excel_to_ADO.Data_Structures.UserStoryData;
import com.example.excel_to_ADO.Excel_Readers.RiskExcelReader;
import com.example.excel_to_ADO.Excel_Readers.UserStoryExcelReader;

public class Main {

    public static void main(String[] args) {

        // Load config from the properties file
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
        } catch (IOException e) {
            System.err.println("✖ Failed to load config.properties: " + e.getMessage());
            return;
        }

        // Read config values
        final String organisationName = props.getProperty("organisationName");
        final String projectName = props.getProperty("projectName");
        final String apiVersion = props.getProperty("apiVersion");
        final String personalAccessToken = props.getProperty("personalAccessToken");
        final String filePath = props.getProperty("filePath");
        final String sheetName = props.getProperty("sheetName");
        
        // Instantiate the client class
        AdoRESTClient client = new AdoRESTClient(organisationName, projectName, apiVersion, personalAccessToken);

        // Instantiate a scanner 
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Excel to Azure Dev Ops uploaded. To upload User Stories press 1, to upload FMEA Risks press 2");
        Integer choice = scanner.nextInt();

        // Check whether the connection is succesfull 
        if(client.testConnection()){

            switch(choice){
                // For User Stories
                case (1): 
                    // Carry out the data retrieval from the excel sheet
                    UserStoryExcelReader userStoryReader = new UserStoryExcelReader(filePath, sheetName);
                    // Extract the sheetData to a local list
                    List<UserStoryData> userStoryData = userStoryReader.getSheetData();

                    // Add each row to dev ops
                    for(UserStoryData row : userStoryData){
                        // "Hierarchy-Reverse" is when each row is a child to the parent. 
                        client.createUserStoryWI(row.childTitle(), row.acceptanceCriteria(), row.parentId(), "Hierarchy-Reverse");
                    }
                // For FMEA Risks
                case (2):
                    // Carry out the data retrieval from the excel sheet
                    RiskExcelReader riskReader = new RiskExcelReader(filePath, sheetName);
                    // Extract the sheetData to a local list
                    List<FMEARiskData> riskData = riskReader.getSheetData();

                    // Add each row to dev ops
                    for(FMEARiskData row : riskData){
                        // "Hierarchy-Reverse" is when each row is a child to the parent. 
                        client.createFMEARiskWI(
                            row.title(), 
                            row.failureEffects(), 
                            row.cause(), 
                            row.preSeverity(), 
                            row.preOccurrence(), 
                            row.preDetection(), 
                            row.mitigationType(), 
                            row.evidence(), 
                            row.resSeverity(),
                            row.resOccurrence(),
                            row.resDetection(),
                            row.predecessorID(),
                            "Dependency-Reverse");
                    }
            }
        }
    }
}
