package com.example.myapplication.api;

import com.example.myapplication.api.responses.GooglePlacesAutocompleteResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * API Service for Google Places Autocomplete API
 */
public interface GooglePlacesApiService {
    
    /**
     * Get place autocomplete predictions using Google Places API
     * @param input The text string on which to search
     * @param key Google API key
     * @return GooglePlacesAutocompleteResponse with predictions
     */
    @GET("place/autocomplete/json")
    Call<GooglePlacesAutocompleteResponse> getPlacePredictions(
            @Query("input") String input,
            @Query("key") String key
    );
}

