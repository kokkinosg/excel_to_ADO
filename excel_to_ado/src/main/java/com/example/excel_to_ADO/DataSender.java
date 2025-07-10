package com.example.excel_to_ADO;
import java.io.IOException;

import org.AzDClientApi;


public class DataSender {
    
    //#region Private variables

    // URL for the Alpaca API
    String organizationName = "SphereFludics";
    String projectName = "Cyto-Mine";

    // Personal access token obtained from User Settings on ADO - valid for 30 days (obtained: 10/07/2025)
    private static final String personalAccessToken = "G4brTLIy1dgtD8J2yBVrdH6ldZOKuD4Wst0xiuelDEyQFRTyxsRHJQQJ99BGACAAAAArd3bnAAASAZDOrJ91";
    

    //#endregion

    //#region Constructor

    // Constructor 

    public DataSender() {

        // Obtain an instance of the ADO api 
        var webApi = new AzDClientApi(organizationName, projectName, personalAccessToken);

    }
    //#endregion

    //#region Public methods
