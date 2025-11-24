package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class EventDetails implements Serializable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("url")
    private String url;
    
    @SerializedName("images")
    private List<Event.Image> images;
    
    @SerializedName("dates")
    private Event.Dates dates;
    
    @SerializedName("classifications")
    private List<Event.Classification> classifications;
    
    @SerializedName("seatmap")
    private Seatmap seatmap;
    
    @SerializedName("ticketing")
    private Ticketing ticketing;
    
    @SerializedName("_embedded")
    private Embedded embedded;
    
    @SerializedName("isFavorited")
    private boolean isFavorited;
    
    // Nested classes
    public static class Seatmap implements Serializable {
        @SerializedName("staticUrl")
        private String staticUrl;
        
        public String getStaticUrl() {
            return staticUrl;
        }
    }
    
    public static class Ticketing implements Serializable {
        @SerializedName("url")
        private String url;
        
        public String getUrl() {
            return url;
        }
    }
    
    public static class Embedded implements Serializable {
        @SerializedName("venues")
        private List<Venue> venues;
        
        @SerializedName("attractions")
        private List<Attraction> attractions;
        
        public List<Venue> getVenues() {
            return venues;
        }
        
        public List<Attraction> getAttractions() {
            return attractions;
        }
    }
    
    public static class Attraction implements Serializable {
        @SerializedName("name")
        private String name;
        
        @SerializedName("id")
        private String id;
        
        @SerializedName("url")
        private String url;
        
        @SerializedName("images")
        private List<Event.Image> images;
        
        public String getName() {
            return name;
        }
        
        public String getId() {
            return id;
        }
        
        public String getUrl() {
            return url;
        }
        
        public List<Event.Image> getImages() {
            return images;
        }
    }
    
    // Constructors
    public EventDetails() {}
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public List<Event.Image> getImages() {
        return images;
    }
    
    public void setImages(List<Event.Image> images) {
        this.images = images;
    }
    
    public Event.Dates getDates() {
        return dates;
    }
    
    public void setDates(Event.Dates dates) {
        this.dates = dates;
    }
    
    public List<Event.Classification> getClassifications() {
        return classifications;
    }
    
    public void setClassifications(List<Event.Classification> classifications) {
        this.classifications = classifications;
    }
    
    public Seatmap getSeatmap() {
        return seatmap;
    }
    
    public void setSeatmap(Seatmap seatmap) {
        this.seatmap = seatmap;
    }
    
    public Ticketing getTicketing() {
        return ticketing;
    }
    
    public void setTicketing(Ticketing ticketing) {
        this.ticketing = ticketing;
    }
    
    public Embedded getEmbedded() {
        return embedded;
    }
    
    public void setEmbedded(Embedded embedded) {
        this.embedded = embedded;
    }
    
    public boolean isFavorited() {
        return isFavorited;
    }
    
    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }
    
    // Helper methods
    public String getSeatmapUrl() {
        if (seatmap != null && seatmap.getStaticUrl() != null) {
            return seatmap.getStaticUrl();
        }
        return null;
    }
    
    public String getTicketingUrl() {
        if (ticketing != null && ticketing.getUrl() != null) {
            return ticketing.getUrl();
        }
        // Fallback to main URL if ticketing URL is not available
        return url;
    }
    
    public String getImageUrl() {
        if (images != null && !images.isEmpty() && images.get(0) != null) {
            return images.get(0).getUrl();
        }
        return null;
    }
    
    public String getCategory() {
        if (classifications != null && !classifications.isEmpty() && 
            classifications.get(0) != null && classifications.get(0).getSegment() != null) {
            return classifications.get(0).getSegment().getName();
        }
        return "Event";
    }
    
    public String getGenre() {
        if (classifications != null && !classifications.isEmpty() && 
            classifications.get(0) != null && classifications.get(0).getGenre() != null) {
            return classifications.get(0).getGenre().getName();
        }
        return "";
    }
    
    public String getGenresString() {
        if (classifications == null || classifications.isEmpty() || classifications.get(0) == null) {
            return "";
        }
        
        Event.Classification classification = classifications.get(0);
        StringBuilder genres = new StringBuilder();
        
        // Order: segment, genre, subGenre, type, subType
        if (classification.getSegment() != null && classification.getSegment().getName() != null 
                && !classification.getSegment().getName().equalsIgnoreCase("Undefined")) {
            genres.append(classification.getSegment().getName());
        }
        
        if (classification.getGenre() != null && classification.getGenre().getName() != null 
                && !classification.getGenre().getName().equalsIgnoreCase("Undefined")) {
            if (genres.length() > 0) genres.append(", ");
            genres.append(classification.getGenre().getName());
        }
        
        if (classification.getSubGenre() != null && classification.getSubGenre().getName() != null 
                && !classification.getSubGenre().getName().equalsIgnoreCase("Undefined")) {
            if (genres.length() > 0) genres.append(", ");
            genres.append(classification.getSubGenre().getName());
        }
        
        if (classification.getType() != null && classification.getType().getName() != null 
                && !classification.getType().getName().equalsIgnoreCase("Undefined")) {
            if (genres.length() > 0) genres.append(", ");
            genres.append(classification.getType().getName());
        }
        
        if (classification.getSubType() != null && classification.getSubType().getName() != null 
                && !classification.getSubType().getName().equalsIgnoreCase("Undefined")) {
            if (genres.length() > 0) genres.append(", ");
            genres.append(classification.getSubType().getName());
        }
        
        return genres.toString();
    }
    
    public String getTicketStatusCode() {
        if (dates != null && dates.getStatus() != null && dates.getStatus().getCode() != null) {
            return dates.getStatus().getCode().toLowerCase();
        }
        return "onsale"; // default
    }
    
    public String getDateTime() {
        if (dates != null && dates.getStart() != null) {
            // Prefer dateTime if available, otherwise use localDate
            if (dates.getStart().getDateTime() != null) {
                return dates.getStart().getDateTime();
            } else if (dates.getStart().getLocalDate() != null) {
                return dates.getStart().getLocalDate();
            }
        }
        return "";
    }
    
    public Venue getVenue() {
        if (embedded != null && embedded.getVenues() != null && 
            !embedded.getVenues().isEmpty() && embedded.getVenues().get(0) != null) {
            return embedded.getVenues().get(0);
        }
        return null;
    }
    
    public String getVenueName() {
        Venue venue = getVenue();
        return venue != null ? venue.getName() : "";
    }
    
    public List<Attraction> getAttractions() {
        if (embedded != null && embedded.getAttractions() != null) {
            return embedded.getAttractions();
        }
        return null;
    }
    
    public String getArtistsString() {
        List<Attraction> attractions = getAttractions();
        if (attractions != null && !attractions.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < attractions.size(); i++) {
                if (i > 0) {
                    sb.append(" | ");
                }
                sb.append(attractions.get(i).getName());
            }
            return sb.toString();
        }
        return "";
    }
}
