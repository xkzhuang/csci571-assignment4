package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class SpotifyAlbum implements Serializable {
    @SerializedName("name")
    private String name;
    
    @SerializedName("release_date")
    private String releaseDate;
    
    @SerializedName("release_date_precision")
    private String releaseDatePrecision;
    
    @SerializedName("total_tracks")
    private int totalTracks;
    
    @SerializedName("images")
    private List<Image> images;
    
    @SerializedName("external_urls")
    private ExternalUrls externalUrls;
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("uri")
    private String uri;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("album_type")
    private String albumType;
    
    // Nested classes
    public static class Image implements Serializable {
        @SerializedName("url")
        private String url;
        
        @SerializedName("height")
        private int height;
        
        @SerializedName("width")
        private int width;
        
        public String getUrl() {
            return url;
        }
        
        public int getHeight() {
            return height;
        }
        
        public int getWidth() {
            return width;
        }
    }
    
    public static class ExternalUrls implements Serializable {
        @SerializedName("spotify")
        private String spotify;
        
        public String getSpotify() {
            return spotify;
        }
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public String getReleaseDate() {
        return releaseDate;
    }
    
    public String getReleaseDatePrecision() {
        return releaseDatePrecision;
    }
    
    public int getTotalTracks() {
        return totalTracks;
    }
    
    public List<Image> getImages() {
        return images;
    }
    
    public String getImageUrl() {
        if (images != null && !images.isEmpty() && images.get(0) != null) {
            return images.get(0).getUrl();
        }
        return null;
    }
    
    public ExternalUrls getExternalUrls() {
        return externalUrls;
    }
    
    public String getSpotifyUrl() {
        if (externalUrls != null) {
            return externalUrls.getSpotify();
        }
        return null;
    }
    
    public String getId() {
        return id;
    }
    
    public String getUri() {
        return uri;
    }
    
    public String getType() {
        return type;
    }
    
    public String getAlbumType() {
        return albumType;
    }
}

