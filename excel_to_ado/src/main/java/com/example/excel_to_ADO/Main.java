package com.example.excel_to_ADO;

public class Main {

    public static void main(String[] args) {

        // Instantiate the data sender class
        adoRESTClient client = new adoRESTClient();

        // Check whether the connection is succesfull 
        client.testConnection();

        // Test the work item ID finder
        String workItemTitle = "It shall be possible to validate Bootstrap camera images for image exposure";
        System.out.printf("ID: %d - Title: %s",client.getWorkItemID(workItemTitle),workItemTitle);
        
    }
    
}
