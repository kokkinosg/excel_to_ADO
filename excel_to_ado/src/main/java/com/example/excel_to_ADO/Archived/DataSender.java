package com.example.excel_to_ADO.Archived;
import org.azd.enums.WorkItemOperation;
import org.azd.exceptions.AzDException;
import org.azd.utils.AzDClientApi;


public class DataSender {
    
    //#region Private variables

    // Private variables for the conenction object
    private final String organizationName = "SphereFluidics";
    private final String projectName = "Cyto-Mine";
    // Personal access token obtained from User Settings on ADO - valid for 30 days (obtained: 10/07/2025)
    // Note: It has full permissions
    private final String personalAccessToken = "66a64rtP0ToGXCOgAlmfFSxPWSIibQv9C309Kxig4TF1sRAgaXWlJQQJ99BGACAAAAArd3bnAAASAZDO2QAl";
    
    // Connection object
    private final AzDClientApi webApi;
    //#endregion

    //#region Constructor

    // Constructor 

    public DataSender() {
        // Obtain an instance of the ADO api only when this class is instantiated
        this.webApi = new AzDClientApi(organizationName, projectName, personalAccessToken);
    }
    //#endregion

    //#region Public methods

    // Connection tested by attempting to get my memberID based on my name
    public boolean isConnected() {
        try {
            var core = webApi.getCoreApi();
            var projects = core.getProjects();
            if (projects != null){
                System.out.println("✔ Connected.");
                return true;
            } else {
                System.out.printf("✖ Not connected. Could not get projects.");
                return  false;
            }
        } catch (AzDException e) {
            System.err.printf("✖ Core call failed: %s", e.getMessage());
            return false;
        }
    }

    // Test creating a work itm
    public boolean createAndDeleteUserStory(){
        // Get an instance of the WorkItemTrackingApi
        var wit = webApi.getWorkItemTrackingApi();
        try {
            wit.createWorkItem("user story", WorkItemOperation.ADD, "GK_TEST_14072025");
            System.out.println("✔ Sample user story added succesfully");
            return true;
        } catch (AzDException e) {
            System.err.printf("✖ Add sample User Story failed: %s", e.getMessage());
            return false;
        }
    }
}
