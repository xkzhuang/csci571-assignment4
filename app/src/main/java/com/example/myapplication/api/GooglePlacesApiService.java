package com.example.myapplication.api;

import com.example.myapplication.api.requests.GooglePlacesAutocompleteRequest;
import com.example.myapplication.api.responses.GooglePlacesV1AutocompleteResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * API Service for Google Places API v1 Autocomplete
 */
public interface GooglePlacesApiService {
    
    /**
     * Get place autocomplete predictions using Google Places API v1
     * @param apiKey Google API key (sent as header)
     * @param request Request body with input and includedPrimaryTypes
     * @return GooglePlacesV1AutocompleteResponse with suggestions
     */
    @POST
    Call<GooglePlacesV1AutocompleteResponse> getPlacePredictions(
            @Url String url,
            @Header("X-Goog-Api-Key") String apiKey,
            @Body GooglePlacesAutocompleteRequest request
    );
}

