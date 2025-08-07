package com.example.excel_to_ADO;

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

    // This includes all logic and CLI code 
    public static void main(String[] args) {

        // Load config from the properties file
        Properties props = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
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

        System.out.printf("\nWelcome to the Excel to Azure Dev Ops uploader. Before continuing, please perform the following checks on Sheet: %s.\n", sheetName);
        System.out.println("-------------------------");
        System.out.println("1. Ensure all columns abide by the data validation rules. No cells in a data validated column should be free text.");
        System.out.println("2. Ensure that no columns are hidden.");
        System.out.println("3. Ensure that all rows are empty after the last row. Try selecting 1000s rows and deleting them.");
        System.out.println("-------------------------");
        System.out.printf("\nTo upload User Stories press 1, to upload FMEA Risks press 2\n");
        Integer choice = scanner.nextInt();

        // Check whether the connection is succesfull 
        if(client.testConnection()){

            switch(choice){
                // For User Stories
                case (1): 
                    // Retrieve all user stories from excel
                    List<UserStoryData> userStories = obtainUserStories(filePath, sheetName);

                    System.out.printf("\nTotal number of User Stories found: %d\n", userStories.size());
                    System.out.printf("Please check that the number matches the rows in excel.\n");

                    if (continueCheck(scanner)){
                        // Upload all risks to Azure DevOps
                        uploadUserStories(userStories, client);
                    } else {
                        System.out.println("Nothing was uploaded.");
                    }
                    break;

                // For FMEA Risks
                case (2):
                    // Obtain all risks from excel file
                    List<FMEARiskData> risks = obtainRisks(filePath,sheetName);

                    System.out.printf("\nTotal number of risks found: %d\n", risks.size());
                    System.out.printf("Please check that the number matches the rows in excel.\n");

                    if (continueCheck(scanner)){
                        // Upload all risks to Azure DevOps
                        uploadRisks(risks, client);
                        System.out.printf("\nUploading FMEA risks finished. Please update excel with the work-item IDs from console\n");
                    } else {
                        System.out.println("Nothing was uploaded.");
                    }
                    break;
                default:
                    System.out.println("Invalid choice, run the program again.");
            }
        }
        scanner.close();
    }

    // Method which handles the logic of obtaining the risks from excel and uploading them to devOps.
    static void uploadRisks(List<FMEARiskData> riskData, AdoRESTClient client){
        // Add each row to dev ops
        for(FMEARiskData row : riskData){
            // Ignore all risks which already have an ADO ID because it means they have already been uploaded
            if(row.riskID()== null) {
                    // "Dependency-Reverse makes the risk a successor to the origin requirement.
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
            } else{
                System.out.printf("Risk ID: %d already exists. Risk not uploaded.\n", row.riskID());
            }
        }
            
    }

    // Method which handles the logic of obtaining the User Stories from excel and uploading them to devOps.
    static void uploadUserStories(List<UserStoryData> userStoryData, AdoRESTClient client){
        // Add each row to dev ops
        for(UserStoryData row : userStoryData){
            // Ignore all user stories which already have an ADO ID because it means they have already been uploaded
            if(row.userStoryId()== null) {
                // "Hierarchy-Reverse" is when each row is a child to the parent. 
                client.createUserStoryWI(
                    row.childTitle(), 
                    row.acceptanceCriteria(), 
                    row.parentId(), 
                    "Hierarchy-Reverse");
            } else {
                System.out.printf("User Story ID: %d already exists. User Story not uploaded.\n", row.userStoryId());
            }
        }
    }

    // Method which handles the logic of obtaining all User stories from excel 
    static List<UserStoryData> obtainUserStories(String filePath, String sheetName){
        // Carry out the data retrieval from the excel sheet
        UserStoryExcelReader userStoryReader = new UserStoryExcelReader(filePath, sheetName);
        // Extract the sheetData to a local list
        return userStoryReader.getSheetData();
    }

    // Method which handles the logic of obtaining all User stories from excel 
    static List<FMEARiskData> obtainRisks(String filePath, String sheetName){
        // Carry out the data retrieval from the excel sheet
        RiskExcelReader riskReader = new RiskExcelReader(filePath, sheetName);
        // Extract the sheetData to a local list
        return riskReader.getSheetData();
    }

    // Method which checks user input and if appropriate to conitnue returns true
    static boolean continueCheck(Scanner scanner){
        System.out.printf("To continue please type y...\n");
        String input = scanner.next().trim().toLowerCase();
        if (!input.equals("y")) {
            System.out.println("Operation cancelled.");
            return false;
        }
        else return true;

    }
}


