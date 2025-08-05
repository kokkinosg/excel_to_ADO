package com.example.excel_to_ADO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// This is the class containing all GET, POST, PUT, DELETE requests from the REST api of Azure Dev Ops. 
// Check out https://learn.microsoft.com/en-us/rest/api/azure/devops/?view=azure-devops-rest-7.2
public class AdoRESTClient {
    
     //#region Private variables

    // Private variables for the conenction object
    private String organisationName;
    private String projectName;
    private String apiVersion;
    // Personal access token obtained from User Settings on ADO - valid for 30 days (obtained: 10/07/2025)
    // Note: It has full permissions
    private String personalAccessToken;
    
    // Client object
    private final OkHttpClient client;
    //#endregion

    // Constructor to create all instances na

     public AdoRESTClient(String organisationName, String projectName, String apiVersion, String personalAccessToken) {
        this.organisationName = organisationName;
        this.projectName = projectName;
        this.apiVersion = apiVersion;
        this.personalAccessToken = personalAccessToken;

        // OKHttpClient with a set timeout of 10s. It will retry connection once if it fails hte first time.
        this.client = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(10))
                .retryOnConnectionFailure(true)
                .build();
    }

    //#region Public functions

    // Method to test the connection
    public boolean testConnection() {
        //Create a URL
        HttpUrl url = HttpUrl.parse(String.format(
                "https://dev.azure.com/%s/_apis/projects?api-version=%s", organisationName, apiVersion));

        // Build a request where each call returns the builder. Thats why i can. simply chain calls. 
        Request req = new Request.Builder() // Build an empty object
                .url(url) // Use the full end point URL built above
                .addHeader("Authorization", Credentials.basic("", personalAccessToken)) // Authorisation header. Note DEVOPS ignors UN so pass an empty string 
                .addHeader("Accept", "application/json") // Tell it that we are expecting a response
                .get() // THis is the HTTP method which is a GET
                .build(); // Build an immutable object to be sent. 
        
        // Test the reponse
        try (Response resp = client.newCall(req).execute()) {

            // If we cannot make the call, print the error message
            if (!resp.isSuccessful()) {
                System.err.printf("✖ HTTP %d %s %n", resp.code(), resp.body() != null ? resp.body().string() : "<no body>");
                return false;
            }
            // If we are succesfull then analyse the response body 
            String body;
            if (resp.body() != null) {
                body = resp.body().string();   // read the response stream to a String
            } else {
                body = "{}";                   // fall back to an empty JSON object literal
            }

            // Uses Gson’s JsonParser to turn the textual JSON in body into a structured JsonObject we can query
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            // Get some sample data. Here we get the number of projects sible to the caller 
            int count;
            if(json.has("count")){
                count = json.get("count").getAsInt();
            } else {
                count = -1;
            }

            System.out.printf("✔ Connected succesfully! %d projects visible for this PAT%n", count);
            return true;
        } catch (IOException e) {
            System.err.printf("✖ Connection error. %s%n", e.getMessage());
            return false;
        }
    }

    // Method to obtain the ID of a workitem using its exact title and a query.
    public Integer getWorkItemID(String workItemTitle){
        // Create the URL
        HttpUrl url = HttpUrl.parse(String.format(
                "https://dev.azure.com/%s/%s/_apis/wit/wiql?api-version=%s",
                organisationName, projectName, apiVersion));

        // Build a query (WIQL) – escape single quotes for safety
        String wiql = String.format(
                "SELECT [System.Id] FROM WorkItems WHERE [System.TeamProject] = '%s' AND [System.Title] = '%s'",
                projectName.replace("'", "''"),
                workItemTitle.replace("'", "''"));

        
        // Create a JSONObject with the query as a property. 
        JsonObject body = new JsonObject();
        body.addProperty("query", wiql);

        // Build a POST request object and include the query. 
        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", Credentials.basic("", personalAccessToken))
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString().getBytes(StandardCharsets.UTF_8)))
                .build();

        
        // Send the request and get a response
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                System.err.printf("✖ Get Work-item ID query failed - HTTP %d %s%n", resp.code(),
                        resp.body() != null ? resp.body().string() : "<no body>");
                return null;
            }

            // If we are succesfull then analyse the response body 
            String respBody;
            if (resp.body() != null) {
                respBody = resp.body().string();   // read the response stream to a String
            } else {
                respBody = "{}";                   // fall back to an empty JSON object literal
            }

            // Uses Gson’s JsonParser to turn the textual JSON in body into a structured JsonObject we can query
            JsonObject json = JsonParser.parseString(respBody).getAsJsonObject();
            // Check if there is a workItems field in the JSon, if not return false
            if (!json.has("workItems")){
                return null;
            } else {
                var arr = json.getAsJsonArray("workItems");
                if (arr.size() == 0) return null;
                int ID = arr.get(0).getAsJsonObject().get("id").getAsInt();
                System.out.println("✔ Method to retrieve WI ID by Title completed succesfully!");
                return ID;
            }
        } catch (IOException e) {
            System.err.printf("✖ Query error. %s%n", e.getMessage());
            return null;
        }
    }


    // Method to create a User Story Requirement work item. It accepts a title, acceptance criteria and specific links to other work items. 
    public boolean createUserStoryWI(String title, String acceptanceCriteria, Integer relatedID, String relationType){

        // Change the type so that it matches what devops expects.
        //String type = URLEncoder.encode("User-Story", StandardCharsets.UTF_8).replace("-", "%20");
        String type = "User%20Story";
        
        // Create the URL
        HttpUrl url = HttpUrl.parse(String.format(
                "https://dev.azure.com/%s/%s/_apis/wit/workitems/$%s?api-version=%s",
                organisationName, projectName, type, apiVersion));

        // Invoke the helper function to create the patch for the request. 
        // JSON Patch is still JSON, but with a special structure: an array of {op, path, value} objects.
        JsonArray patch = createUserStoryJsonArray(title, acceptanceCriteria, relatedID, relationType);

        // Invoke the createWorkItem helper to build a post request and analyse the response to see if items were succesfully created
        return createWorkItem(url, patch, "User-Story");
    }

    // Method to create a FMEA Risk work item. 
    public boolean createFMEARiskWI(String title, String failureEffects, String cause,
        String preSeverity, String preOccurrence, String preDetection,
        String mitigation, String evidence, 
        String resSeverity, String resOccurrence, String resDetection,
        Integer relatedID, String relationType){

        // Change the type so that it matches what devops expects.
        //String type = URLEncoder.encode("FMEA-Risk", StandardCharsets.UTF_8).replace("-", "%20");
        String type = "FMEA%20Risk";
        
        // Create the URL
        HttpUrl url = HttpUrl.parse(String.format(
                "https://dev.azure.com/%s/%s/_apis/wit/workitems/$%s?api-version=%s",
                organisationName, projectName, type, apiVersion));

        // Invoke the helper function to create the patch for the request. 
        // JSON Patch is still JSON, but with a special structure: an array of {op, path, value} objects.
        JsonArray patch = createRiskJsonArray(title, failureEffects, cause, mitigation, evidence, preSeverity, preOccurrence, preDetection, resSeverity, resOccurrence, resDetection, relatedID, relationType);

        // Invoke the createWorkItem helper to build a post request and analyse the response to see if items were succesfully created
        return createWorkItem(url, patch, "FMEA-Risk");
    }
    
    //#endregion

    //#region Helper functions

    // Helper function to create any WorkItem by accepting the appropriate JsonArray
    private boolean createWorkItem(HttpUrl url, JsonArray jsonArray, String workItemType){

        // Build a POST request object and include the query. 
        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", Credentials.basic("", personalAccessToken))
                .addHeader("Content-Type", "application/json-patch+json")
                .post(RequestBody.create(jsonArray.toString().getBytes(StandardCharsets.UTF_8)))
                .build();
        
        try (Response resp = client.newCall(req).execute()) {

            if (!resp.isSuccessful()) {
                System.err.printf("✖ Work-item create failed. HTTP %d %s%n", resp.code(), resp.body() != null ? resp.body().string() : "<no body>");
                return false;
            }

            // If we are succesfull then analyse the response body 
            String body;
            if (resp.body() != null) {
                body = resp.body().string();   // read the response stream to a String
            } else {
                body = "{}";                   // fall back to an empty JSON object literal
            }

            // Create a json object which is interogatable.
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            // Retrieve the ID of the created WI.
            int newId = json.get("id").getAsInt();
            
            System.out.printf("\n✔ Created work-item #%d (%s)\n", newId, workItemType);
            return true;
        } catch (IOException e) {
            System.err.printf("✖ Create error - %s%n", e.getMessage());
            return false;
        }
    }

    // Helper method to create a JSON array object for creatin work items
    // PARENT("System.LinkTypes.Hierarchy-Forward"),
    // CHILD ("System.LinkTypes.Hierarchy-Reverse"),
    // RELATED("System.LinkTypes.Related");   
    private JsonArray createUserStoryJsonArray(String title, String acceptanceCriteria, Integer relatedID, String relationType){
        // Create a JSON Array with the required info 
        JsonArray patch = new JsonArray();

        // Add the tile
        JsonObject opTitle = new JsonObject();
        opTitle.addProperty("op", "add"); // Define the patch operation. Here we are adding. 
        opTitle.addProperty("path", "/fields/System.Title"); // Path for the operation.
        opTitle.addProperty("value", title); // The value for the operation which is an argument to this method. 
        patch.add(opTitle);
        // Acceptance Criteria
        if (acceptanceCriteria != null && !acceptanceCriteria.isBlank()) {
            JsonObject opAC = new JsonObject();
            opAC.addProperty("op", "add");
            opAC.addProperty("path", "/fields/Microsoft.VSTS.Common.AcceptanceCriteria");
            opAC.addProperty("value", acceptanceCriteria);
            patch.add(opAC);
        }
        // Relations
        // Only run this code if the id of WI to be linked is provided 
        if (relatedID != null) {
            // Set the patch operation
            JsonObject linkOp = new JsonObject();
            linkOp.addProperty("op", "add");
            linkOp.addProperty("path", "/relations/-");

            // Add the relationship id
            JsonObject linkVal = new JsonObject();
            // Create a parameterised string to set the relationship value
            String value = "System.LinkTypes." + relationType;
            linkVal.addProperty("rel", value);
            linkVal.addProperty("url", String.format("https://dev.azure.com/%s/_apis/wit/workItems/%d", organisationName, relatedID));
            
            // Add a comment (not necessry)
            JsonObject attrs = new JsonObject();
            attrs.addProperty("comment", "Auto - linked by importer");
            linkVal.add("attributes", attrs);
            linkOp.add("value", linkVal);
            patch.add(linkOp);
        }

        return patch;
    }

    // Helper method to create a JSON array object for creatin work items
    // PARENT("System.LinkTypes.Hierarchy-Forward"),
    // CHILD ("System.LinkTypes.Hierarchy-Reverse"),
    // RELATED("System.LinkTypes.Related");  
    // SUCCESSOR ("System.LinkTypes.Dependency-Reverse"),
    // PREDECCESSOR ("System.LinkTypes.Dependency-Forward");    
    private JsonArray createRiskJsonArray(
        String title, String failureEffects, String cause,
        String mitigation, String evidence, 
        String preSeverity, String preOccurrence, String preDetection, 
        String resSeverity, String resOccurrence, String resDetection,
        Integer relatedID, String relationType)
        {
        // Create a JSON Array with the required info 
        JsonArray patch = new JsonArray();

        // Add the tile - Failure Mode
        JsonObject opTitle = new JsonObject();
        opTitle.addProperty("op", "add"); // Define the patch operation. Here we are adding. 
        opTitle.addProperty("path", "/fields/System.Title"); // Path for the operation.
        opTitle.addProperty("value", title); // The value for the operation which is an argument to this method. 
        patch.add(opTitle);

        // Potential Failure Effects
        if (failureEffects != null && !failureEffects.isBlank()) {
            JsonObject opAC = new JsonObject();
            opAC.addProperty("op", "add");
            opAC.addProperty("path", "/fields/Custom.FMEAPotentialFailureEffects");
            opAC.addProperty("value", failureEffects);
            patch.add(opAC);
        }

        // Probable Causes
        if (cause != null && !cause.isBlank()) {
            JsonObject opAC = new JsonObject();
            opAC.addProperty("op", "add");
            opAC.addProperty("path", "/fields/Custom.FMEAProbableCause");
            opAC.addProperty("value", cause);
            patch.add(opAC);
        }

        // Mitigation Type
        if (mitigation != null && !mitigation.isBlank()) {
            JsonObject opAC = new JsonObject();
            opAC.addProperty("op", "add");
            opAC.addProperty("path", "/fields/Custom.FMEAMitigationType");
            opAC.addProperty("value", mitigation);
            patch.add(opAC);
        }

        // Evidence
        if (evidence != null && !evidence.isBlank()) {
            JsonObject opAC = new JsonObject();
            opAC.addProperty("op", "add");
            opAC.addProperty("path", "/fields/Custom.FMEAEvidence");
            opAC.addProperty("value", evidence);
            patch.add(opAC);
        }

        // Preliminary RIsk Evaluation Severity
        if (preSeverity != null && !preSeverity.isBlank()) {
            JsonObject opAC = new JsonObject();
            opAC.addProperty("op", "add");
            opAC.addProperty("path", "/fields/Custom.FMEASeverity");
            opAC.addProperty("value", preSeverity);
            patch.add(opAC);
        }

        // Preliminary RIsk Evaluation Occurrence
        if (preOccurrence != null && !preOccurrence.isBlank()) {
            JsonObject opAC = new JsonObject();
            opAC.addProperty("op", "add");
            opAC.addProperty("path", "/fields/Custom.FMEAOccurrence");
            opAC.addProperty("value", preOccurrence);
            patch.add(opAC);
        }

        // Preliminary RIsk Evaluation Detection
        if (preDetection != null && !preDetection.isBlank()) {
            JsonObject opAC = new JsonObject();
            opAC.addProperty("op", "add");
            opAC.addProperty("path", "/fields/Custom.FMEADetection");
            opAC.addProperty("value", preDetection);
            patch.add(opAC);
        }

        // Residual Risk Evaluation Severity
        if (resSeverity != null && !resSeverity.isBlank()) {
            JsonObject opAC = new JsonObject();
            opAC.addProperty("op", "add");
            opAC.addProperty("path", "/fields/Custom.FMEAResidualSeverity");
            opAC.addProperty("value", resSeverity);
            patch.add(opAC);
        }

        // Residual RIsk Evaluation Occurrence
        if (resOccurrence != null && !resOccurrence.isBlank()) {
            JsonObject opAC = new JsonObject();
            opAC.addProperty("op", "add");
            opAC.addProperty("path", "/fields/Custom.FMEAResidualOccurrence");
            opAC.addProperty("value", resOccurrence);
            patch.add(opAC);
        }

        // Residual RIsk Evaluation Detection
        if (resDetection != null && !resDetection.isBlank()) {
            JsonObject opAC = new JsonObject();
            opAC.addProperty("op", "add");
            opAC.addProperty("path", "/fields/Custom.FMEAResidualDetection");
            opAC.addProperty("value", resDetection);
            patch.add(opAC);
        }

        // Relations
        // Only run this code if the id of WI to be linked is provided 
        if (relatedID != null) {
            // Set the patch operation
            JsonObject linkOp = new JsonObject();
            linkOp.addProperty("op", "add");
            linkOp.addProperty("path", "/relations/-");

            // Add the relationship id
            JsonObject linkVal = new JsonObject();
            // Create a parameterised string to set the relationship value
            String value = "System.LinkTypes." + relationType;
            linkVal.addProperty("rel", value);
            linkVal.addProperty("url", String.format("https://dev.azure.com/%s/_apis/wit/workItems/%d", organisationName, relatedID));
            
            // Add a comment (not necessry)
            JsonObject attrs = new JsonObject();
            attrs.addProperty("comment", "Auto - linked by importer");
            linkVal.add("attributes", attrs);
            linkOp.add("value", linkVal);
            patch.add(linkOp);
        }

        return patch;
    }

    //#endregion
}
