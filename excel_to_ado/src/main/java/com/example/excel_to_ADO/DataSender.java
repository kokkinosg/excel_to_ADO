package com.example.excel_to_ADO;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DataSender {
    
    //#region Private variables

    // URL for the Alpaca API
    private static final String ALPACA_API_URL = "https://data.alpaca.markets/v2/stocks/";

    // API key and secret for authentication
    private static final String API_KEY = "PKPF2OPXIBBQ9U9RJ86L";
    private static final String API_SECRET = "JuXglAzELlHyIfGuIfG8PwbQfxMeYygTotmjVgpB";
    
    // Create one OkHttpClient instance to be used for all requests because it is thread-safe and can be reused.
    // This is a good practice to avoid creating multiple instances of OkHttpClient, which can lead to resource leaks and performance issues.
    private final OkHttpClient client;
    
    // Gson instance for JSON serialization/deserialization
    private final Gson gson;


    //#endregion

    //#region Constructor

    // Constructor to initialize the DataFetcher with stock marker (e.g. AAPL for apple stocks), time frame, start date, and end date.

    public DataSender() {

        // Initialize the OkHttpClient with a timeout of 30 seconds for both connection and read operations.
        this.client = new OkHttpClient.Builder().callTimeout(Duration.ofSeconds(10)).retryOnConnectionFailure(true).build();

        // Initialise the Gson instance for JSON serialization/deserialization.
        this.gson = new Gson();
    }
    //#endregion

    //#region Public methods

    // Method to retrieve data from the Alpaca API.
    public List<CandleStick> getCandlestickData() {

        // Build a URL for the API request using the provided parameters.
        HttpUrl url = urlBuilder(stockMarker, timeFrame, startDate, endDate);

        // Create a request object using the built URL and authentication headers.
        Request request = createRequest(url);

        // Send the request and get the response as a string.
        String jsonResponse = getResponse(request);

        // If the response is not null, convert the JSON response to a list of CandleStick objects.
        if (jsonResponse != null) {
            System.out.println("Data fetched successfully.");
            candleSticks = convertJSONtoCandleData(jsonResponse);
            System.out.println("There are " + candleSticks.size() + " candlestick data available.");
            return candleSticks;
        } else {
            System.out.println("Failed to fetch data.");
            return null;
        }
    }

    //#endregion

    //#region Helper methods

    // Helper method to create the url for the API request. 
    private HttpUrl urlBuilder(String stockMarker, String timeFrame, String startDate, String endDate) {
        return HttpUrl.parse(ALPACA_API_URL + stockMarker + "/bars")
                .newBuilder()
                .addQueryParameter("timeframe", timeFrame)
                .addQueryParameter("start", startDate)
                .addQueryParameter("end", endDate)
                .build();
    }

    // Helper method to make the API request and handle the response. 
    private Request createRequest(HttpUrl url) {

        // Create the request using OkHttp
        Request request = new Request.Builder()
            .url(url)
            .addHeader("APCA-API-KEY-ID", API_KEY)
            .addHeader("APCA-API-SECRET-KEY", API_SECRET)
            .get()
            .build();
        
        return request;
    }
    
    // Helper method to send a request and get the response.
    private String getResponse(Request request) {

        // Try to get a response from the server.
        try (Response response = client.newCall(request).execute()) {
            // Check if the response is successful (HTTP status code 200)
            // If not, throw an IOException with the unexpected response code.
            if (!response.isSuccessful()) {
                
                // Get the response code, eg. 200, 201, 400, 401 etc. 
                int code = response.code();

                // Throw the IOException with the response code and message.
                throw new IOException("HTTP " + code + " - " + response.body().string());
            } else {
                // If the response is successful, return the response body as a string.
                return response.body().string();
            }

        } catch (Exception e) {
            // If any other exception occurs print the stack trace and return null.
            // This could be a network error, JSON parsing error, etc.
            e.printStackTrace();
            return null;
        }
    }
}
