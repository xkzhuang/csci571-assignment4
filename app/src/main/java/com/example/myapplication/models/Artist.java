package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Artist implements Serializable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("image")
    private String imageUrl;
    
    @SerializedName("url")
    private String url;
    
    @SerializedName("followers")
    private int followers;
    
    @SerializedName("popularity")
    private int popularity;
    
    @SerializedName("genres")
    private List<String> genres;
    
    @SerializedName("albums")
    private List<Album> albums;
    
    // Constructors
    public Artist() {}
    
    public Artist(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }
    
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
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public int getFollowers() {
        return followers;
    }
    
    public void setFollowers(int followers) {
        this.followers = followers;
    }
    
    public int getPopularity() {
        return popularity;
    }
    
    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }
    
    public List<String> getGenres() {
        return genres;
    }
    
    public void setGenres(List<String> genres) {
        this.genres = genres;
    }
    
    public List<Album> getAlbums() {
        return albums;
    }
    
    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }
    
    // Nested Album class
    public static class Album implements Serializable {
        @SerializedName("id")
        private String id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("image")
        private String imageUrl;
        
        @SerializedName("releaseDate")
        private String releaseDate;
        
        public Album() {}
        
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
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        public String getReleaseDate() {
            return releaseDate;
        }
        
        public void setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
        }
    }
}

