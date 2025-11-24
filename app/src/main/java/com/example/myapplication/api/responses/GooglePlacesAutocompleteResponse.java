package com.example.myapplication.api.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response model for Google Places Autocomplete API
 */
public class GooglePlacesAutocompleteResponse {
    
    @SerializedName("predictions")
    private List<Prediction> predictions;
    
    @SerializedName("status")
    private String status;
    
    public List<Prediction> getPredictions() {
        return predictions;
    }
    
    public String getStatus() {
        return status;
    }
    
    public boolean isSuccessful() {
        return "OK".equals(status) || "ZERO_RESULTS".equals(status);
    }
    
    /**
     * Prediction model for autocomplete results
     */
    public static class Prediction {
        @SerializedName("description")
        private String description;
        
        @SerializedName("place_id")
        private String placeId;
        
        @SerializedName("structured_formatting")
        private StructuredFormatting structuredFormatting;
        
        public String getDescription() {
            return description;
        }
        
        public String getPlaceId() {
            return placeId;
        }
        
        public StructuredFormatting getStructuredFormatting() {
            return structuredFormatting;
        }
    }
    
    /**
     * Structured formatting for predictions
     */
    public static class StructuredFormatting {
        @SerializedName("main_text")
        private String mainText;
        
        @SerializedName("secondary_text")
        private String secondaryText;
        
        public String getMainText() {
            return mainText;
        }
        
        public String getSecondaryText() {
            return secondaryText;
        }
    }
}

