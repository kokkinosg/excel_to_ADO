package com.example.excel_to_ADO;

public class Main {

    public static void main(String[] args) {

        // Instantiate the client class
        adoRESTClient client = new adoRESTClient();

        // Check whether the connection is succesfull 
        if(client.testConnection()){
            // Test the work item ID finder
            // String workItemTitle = "It shall be possible to validate Bootstrap camera images for image exposure";
            // System.out.printf("ID: %d - Title: %s",client.getWorkItemID(workItemTitle),workItemTitle);

            // Create a sample work item
            client.createWorkItem("User-Story", "TEST_PARENT_GK_1407025", "sample acceptance criteria", null, null);
            // Get its ID
            Integer id = client.getWorkItemID("TEST_PARENT_GK_1407025");
            // Create a child of the sample work item using Hierarchy-Reverse
            client.createWorkItem("User-Story", "TEST_CHILD_GK_1407025", "sample acceptance criteria", id, "Hierarchy-Reverse");
            
        }

    }
    
}
