package com.example.excel_to_ADO;
import org.azd.exceptions.AzDException;
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

    // Connection tested by attempting to get my memberID based on my name
    public boolean isConnectionSuccessful(){
        try {
            var mem = webApi.getMemberEntitlementManagementApi();

            String memberId = mem
                    .getUserEntitlements()
                    .getMembers()
                    .stream()
                    .filter(x -> x.getUser().getDisplayName().contains("Kokkinos"))
                    .findFirst()
                    .get()
                    .getId();
            System.out.printf("Connection successful. Georgios Kokkinos Member ID is %s", memberId);
            return true;
        } catch (AzDException e) {
            System.out.printf("Connection failed! \n %s\n", e.getMessage());
            return false;
        }
    }

}
