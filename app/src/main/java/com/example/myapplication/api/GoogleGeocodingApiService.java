package com.example.myapplication.api;

import com.example.myapplication.api.responses.GoogleGeocodingResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * API Service for direct calls to Google Geocoding API
 */
public interface GoogleGeocodingApiService {
    
    /**
     * Geocode an address using Google Geocoding API
     * @param address Address to geocode
     * @param key Google API key
     * @return GoogleGeocodingResponse with coordinates
     */
    @GET("geocode/json")
    Call<GoogleGeocodingResponse> geocodeAddress(
            @Query("address") String address,
            @Query("key") String key
    );
}

