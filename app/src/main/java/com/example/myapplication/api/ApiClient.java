package com.example.myapplication.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://xkzhuang-csci571-hw03-820224420769.us-west1.run.app/";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    
    // Use BASIC level instead of BODY for performance (logs headers only, not body)
    // Set to NONE in production builds for maximum performance
    private static final HttpLoggingInterceptor.Level LOG_LEVEL = HttpLoggingInterceptor.Level.BASIC;
    
    private ApiClient() {
        // Private constructor to prevent instantiation
    }
    
    public static ApiService getApiService() {
        if (apiService == null) {
            synchronized (ApiClient.class) {
                if (apiService == null) {
                    retrofit = createRetrofit();
                    apiService = retrofit.create(ApiService.class);
                }
            }
        }
        return apiService;
    }
    
    private static Retrofit createRetrofit() {
        // Create logging interceptor - use BASIC instead of BODY for performance
        // BODY level logs entire request/response which is extremely slow and memory intensive
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        // Use BASIC level (logs headers only) instead of BODY (logs everything)
        // For production, change LOG_LEVEL to NONE
        loggingInterceptor.setLevel(LOG_LEVEL);
        
        // Create optimized OkHttp client with connection pooling and timeouts
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS) // Connection timeout
                .readTimeout(30, TimeUnit.SECONDS)   // Read timeout
                .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
                .retryOnConnectionFailure(true)       // Retry on connection failure
                .build();
        
        // Create lenient Gson instance that handles nulls and missing fields
        Gson gson = new GsonBuilder()
                .setLenient()
                .serializeNulls()
                .create();
        
        // Create Retrofit instance
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    
    public static void resetInstance() {
        retrofit = null;
        apiService = null;
    }
}

