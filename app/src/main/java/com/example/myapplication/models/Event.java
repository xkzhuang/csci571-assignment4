package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Event implements Serializable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("url")
    private String url;
    
    @SerializedName("isFavorited")
    private boolean isFavorited;
    
    @SerializedName("utc")
    private String utc; // UTC timestamp for when added/updated to favorites
    
    @SerializedName("images")
    private List<Image> images;
    
    @SerializedName("classifications")
    private List<Classification> classifications;
    
    @SerializedName("dates")
    private Dates dates;
    
    @SerializedName("_embedded")
    private Embedded embedded;
    
    // Nested classes
    public static class Image implements Serializable {
        @SerializedName("url")
        private String url;
        
        @SerializedName("ratio")
        private String ratio;
        
        public String getUrl() {
            return url;
        }
    }
    
    public static class Classification implements Serializable {
        @SerializedName("segment")
        private Segment segment;
        
        @SerializedName("genre")
        private Genre genre;
        
        @SerializedName("subGenre")
        private SubGenre subGenre;
        
        @SerializedName("type")
        private Type type;
        
        @SerializedName("subType")
        private SubType subType;
        
        public Segment getSegment() {
            return segment;
        }
        
        public Genre getGenre() {
            return genre;
        }
        
        public SubGenre getSubGenre() {
            return subGenre;
        }
        
        public Type getType() {
            return type;
        }
        
        public SubType getSubType() {
            return subType;
        }
        
        public static class Segment implements Serializable {
            @SerializedName("name")
            private String name;
            
            public String getName() {
                return name;
            }
        }
        
        public static class Genre implements Serializable {
            @SerializedName("name")
            private String name;
            
            public String getName() {
                return name;
            }
        }
        
        public static class SubGenre implements Serializable {
            @SerializedName("name")
            private String name;
            
            public String getName() {
                return name;
            }
        }
        
        public static class Type implements Serializable {
            @SerializedName("name")
            private String name;
            
            public String getName() {
                return name;
            }
        }
        
        public static class SubType implements Serializable {
            @SerializedName("name")
            private String name;
            
            public String getName() {
                return name;
            }
        }
    }
    
    public static class Dates implements Serializable {
        @SerializedName("start")
        private Start start;
        
        @SerializedName("status")
        private Status status;
        
        public Start getStart() {
            return start;
        }
        
        public Status getStatus() {
            return status;
        }
        
        public static class Start implements Serializable {
            @SerializedName("dateTime")
            private String dateTime;
            
            @SerializedName("localDate")
            private String localDate;
            
            @SerializedName("timeTBA")
            private boolean timeTBA;
            
            @SerializedName("noSpecificTime")
            private boolean noSpecificTime;
            
            public String getDateTime() {
                return dateTime;
            }
            
            public String getLocalDate() {
                return localDate;
            }
            
            public boolean isTimeTBA() {
                return timeTBA;
            }
            
            public boolean isNoSpecificTime() {
                return noSpecificTime;
            }
        }
        
        public static class Status implements Serializable {
            @SerializedName("code")
            private String code;
            
            public String getCode() {
                return code;
            }
        }
    }
    
    public static class Embedded implements Serializable {
        @SerializedName("venues")
        private List<Venue> venues;
        
        public List<Venue> getVenues() {
            return venues;
        }
    }
    
    // Constructors
    public Event() {}
    
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
    
    public List<Image> getImages() {
        return images;
    }
    
    public void setImages(List<Image> images) {
        this.images = images;
    }
    
    public List<Classification> getClassifications() {
        return classifications;
    }
    
    public void setClassifications(List<Classification> classifications) {
        this.classifications = classifications;
    }
    
    public Dates getDates() {
        return dates;
    }
    
    public void setDates(Dates dates) {
        this.dates = dates;
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
    
    public String getUtc() {
        return utc;
    }
    
    public void setUtc(String utc) {
        this.utc = utc;
    }
    
    // Helper methods to get nested data safely
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
        return "Unknown";
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
    
    public boolean hasSpecificTime() {
        if (dates != null && dates.getStart() != null) {
            return !dates.getStart().isTimeTBA() && !dates.getStart().isNoSpecificTime();
        }
        return false;
    }
    
    public String getVenue() {
        if (embedded != null && embedded.getVenues() != null && 
            !embedded.getVenues().isEmpty() && embedded.getVenues().get(0) != null) {
            return embedded.getVenues().get(0).getName();
        }
        return "";
    }
    
    public String getTicketStatusCode() {
        if (dates != null && dates.getStatus() != null && dates.getStatus().getCode() != null) {
            return dates.getStatus().getCode().toLowerCase();
        }
        return "onsale"; // default
    }
}

