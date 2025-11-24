package com.example.myapplication.api.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response model for Google Places API v1 Autocomplete
 */
public class GooglePlacesV1AutocompleteResponse {
    
    @SerializedName("suggestions")
    private List<Suggestion> suggestions;
    
    public List<Suggestion> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }
    
    /**
     * Suggestion model
     */
    public static class Suggestion {
        @SerializedName("placePrediction")
        private PlacePrediction placePrediction;
        
        public PlacePrediction getPlacePrediction() {
            return placePrediction;
        }
        
        public void setPlacePrediction(PlacePrediction placePrediction) {
            this.placePrediction = placePrediction;
        }
    }
    
    /**
     * PlacePrediction model
     */
    public static class PlacePrediction {
        @SerializedName("text")
        private Text text;
        
        public Text getText() {
            return text;
        }
        
        public void setText(Text text) {
            this.text = text;
        }
    }
    
    /**
     * Text model containing the place name
     */
    public static class Text {
        @SerializedName("text")
        private String text;
        
        @SerializedName("matches")
        private List<Match> matches;
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public List<Match> getMatches() {
            return matches;
        }
        
        public void setMatches(List<Match> matches) {
            this.matches = matches;
        }
    }
    
    /**
     * Match model for highlighting matched text
     */
    public static class Match {
        @SerializedName("endOffset")
        private Integer endOffset;
        
        public Integer getEndOffset() {
            return endOffset;
        }
        
        public void setEndOffset(Integer endOffset) {
            this.endOffset = endOffset;
        }
    }
}

