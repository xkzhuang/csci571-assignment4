package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class SpotifyArtist implements Serializable {
    @SerializedName("name")
    private String name;
    
    @SerializedName("followers")
    private Followers followers;
    
    @SerializedName("popularity")
    private int popularity;
    
    @SerializedName("images")
    private List<Image> images;
    
    @SerializedName("external_urls")
    private ExternalUrls externalUrls;
    
    @SerializedName("genres")
    private List<String> genres;
    
    // Nested classes
    public static class Followers implements Serializable {
        @SerializedName("total")
        private int total;
        
        public int getTotal() {
            return total;
        }
    }
    
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
    
    public Followers getFollowers() {
        return followers;
    }
    
    public int getFollowersCount() {
        return followers != null ? followers.getTotal() : 0;
    }
    
    public int getPopularity() {
        return popularity;
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
    
    public List<String> getGenres() {
        return genres;
    }
}

