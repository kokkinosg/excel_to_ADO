package com.example.excel_to_ADO;
import org.azd.utils.AzDClientApi;


public class DataSender {
    
    //#region Private variables

    // Private variables for the conenction object
    private final String organizationName = "SphereFludics";
    private final String projectName = "Cyto-Mine";
    // Personal access token obtained from User Settings on ADO - valid for 30 days (obtained: 10/07/2025)
    // Note: It only has permissions to read and write workitems. 
    private static final String personalAccessToken = "G4brTLIy1dgtD8J2yBVrdH6ldZOKuD4Wst0xiuelDEyQFRTyxsRHJQQJ99BGACAAAAArd3bnAAASAZDOrJ91";
    
    // Connection object
    private AzDClientApi webApi;
    //#endregion

    //#region Constructor

    // Constructor 

    public DataSender() {
        // Obtain an instance of the ADO api only when this class is instantiated
        this.webApi = new AzDClientApi(organizationName, projectName, personalAccessToken);
    }
    //#endregion

    //#region Public methods

}
