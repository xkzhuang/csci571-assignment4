package com.example.myapplication.api.requests;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Request model for Google Places API v1 Autocomplete
 */
public class GooglePlacesAutocompleteRequest {
    
    @SerializedName("input")
    private String input;
    
    @SerializedName("includedPrimaryTypes")
    private List<String> includedPrimaryTypes;
    
    public GooglePlacesAutocompleteRequest(String input) {
        this.input = input;
        this.includedPrimaryTypes = new ArrayList<>();
        this.includedPrimaryTypes.add("(cities)");
    }
    
    public String getInput() {
        return input;
    }
    
    public void setInput(String input) {
        this.input = input;
    }
    
    public List<String> getIncludedPrimaryTypes() {
        return includedPrimaryTypes;
    }
    
    public void setIncludedPrimaryTypes(List<String> includedPrimaryTypes) {
        this.includedPrimaryTypes = includedPrimaryTypes;
    }
}

