package com.example.excel_to_ADO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

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
public class adoRESTClient {
    
     //#region Private variables

    // Private variables for the conenction object
    private final String organisationName = "SphereFluidics";
    private final String projectName = "Cyto-Mine";
    private final String apiVersion = "7.1";
    // Personal access token obtained from User Settings on ADO - valid for 30 days (obtained: 10/07/2025)
    // Note: It has full permissions
    private final String personalAccessToken = "66a64rtP0ToGXCOgAlmfFSxPWSIibQv9C309Kxig4TF1sRAgaXWlJQQJ99BGACAAAAArd3bnAAASAZDO2QAl";
    
    // Client object
    private final OkHttpClient client;
    //#endregion

    // Constructor to create all instances

     public adoRESTClient() {

        // OKHttpClient with a set timeout of 10s. It will retry connection once if it fails hte first time.
        this.client = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(10))
                .retryOnConnectionFailure(true)
                .build();
    }

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

    // Method to create obtain the ID of a workitem using its exact title and a querry.
    public Integer getWorkItemID(String workItemTitle){
        // Create the URL
        HttpUrl url = HttpUrl.parse(String.format(
                "https://dev.azure.com/%s/%s/_apis/wit/wiql?api-version=7.1",
                organisationName, projectName));

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

}
