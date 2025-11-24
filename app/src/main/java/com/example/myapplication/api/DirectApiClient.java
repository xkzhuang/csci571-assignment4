package com.example.myapplication.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API Client for direct external API calls (ipinfo.io and Google Geocoding)
 */
public class DirectApiClient {
    private static final String IPINFO_BASE_URL = "https://ipinfo.io/";
    private static final String GOOGLE_GEOCODING_BASE_URL = "https://maps.googleapis.com/maps/api/";
    private static final String GOOGLE_PLACES_API_BASE_URL = "https://places.googleapis.com/v1/";
    
    private static IpInfoApiService ipInfoApiService = null;
    private static GoogleGeocodingApiService googleGeocodingApiService = null;
    private static GooglePlacesApiService googlePlacesApiService = null;
    
    private DirectApiClient() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Get IpInfo API service
     * @return IpInfoApiService instance
     */
    public static IpInfoApiService getIpInfoService() {
        if (ipInfoApiService == null) {
            synchronized (DirectApiClient.class) {
                if (ipInfoApiService == null) {
                    Retrofit retrofit = createRetrofit(IPINFO_BASE_URL);
                    ipInfoApiService = retrofit.create(IpInfoApiService.class);
                }
            }
        }
        return ipInfoApiService;
    }
    
    /**
     * Get Google Geocoding API service
     * @return GoogleGeocodingApiService instance
     */
    public static GoogleGeocodingApiService getGoogleGeocodingService() {
        if (googleGeocodingApiService == null) {
            synchronized (DirectApiClient.class) {
                if (googleGeocodingApiService == null) {
                    Retrofit retrofit = createRetrofit(GOOGLE_GEOCODING_BASE_URL);
                    googleGeocodingApiService = retrofit.create(GoogleGeocodingApiService.class);
                }
            }
        }
        return googleGeocodingApiService;
    }
    
    /**
     * Get Google Places API service
     * @return GooglePlacesApiService instance
     */
    public static GooglePlacesApiService getGooglePlacesService() {
        if (googlePlacesApiService == null) {
            synchronized (DirectApiClient.class) {
                if (googlePlacesApiService == null) {
                    Retrofit retrofit = createPlacesRetrofit();
                    googlePlacesApiService = retrofit.create(GooglePlacesApiService.class);
                }
            }
        }
        return googlePlacesApiService;
    }
    
    // Use BASIC level instead of BODY for performance (logs headers only, not body)
    // Set to NONE in production builds for maximum performance
    private static final HttpLoggingInterceptor.Level LOG_LEVEL = HttpLoggingInterceptor.Level.BASIC;
    
    /**
     * Create Retrofit instance with the given base URL
     * @param baseUrl Base URL for the API
     * @return Retrofit instance
     */
    private static Retrofit createRetrofit(String baseUrl) {
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
        
        // Create lenient Gson instance
        Gson gson = new GsonBuilder()
                .setLenient()
                .serializeNulls()
                .create();
        
        // Create Retrofit instance
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    
    /**
     * Create Retrofit instance for Google Places API v1 with custom headers
     * @return Retrofit instance
     */
    private static Retrofit createPlacesRetrofit() {
        // Create logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(LOG_LEVEL);
        
        // Create interceptor to add API key and field mask header
        Interceptor headerInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("X-Goog-FieldMask", "suggestions.placePrediction.text");
            
            Request request = requestBuilder.build();
            return chain.proceed(request);
        };
        
        // Create optimized OkHttp client with connection pooling and timeouts
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS) // Connection timeout
                .readTimeout(30, TimeUnit.SECONDS)   // Read timeout
                .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
                .retryOnConnectionFailure(true)       // Retry on connection failure
                .build();
        
        // Create lenient Gson instance
        Gson gson = new GsonBuilder()
                .setLenient()
                .serializeNulls()
                .create();
        
        // Create Retrofit instance
        return new Retrofit.Builder()
                .baseUrl(GOOGLE_PLACES_API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    
    /**
     * Reset API service instances (for testing)
     */
    public static void resetInstances() {
        ipInfoApiService = null;
        googleGeocodingApiService = null;
        googlePlacesApiService = null;
    }
}

