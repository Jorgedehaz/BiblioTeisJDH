package com.example.biblioteca.API.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = "http://10.0.2.2:5097/api/";  // Replace with your actual base URL

    // Get the Retrofit client instance
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())  // This converts the API response to Java objects
                    .build();
        }
        return retrofit;
    }
}
