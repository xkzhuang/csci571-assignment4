package com.example.myapplication.api;

import com.example.myapplication.models.Artist;
import com.example.myapplication.models.Event;
import com.example.myapplication.models.EventDetails;
import com.example.myapplication.api.responses.SearchResponse;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    
    /**
     * Search for events
     * @param keyword Search keyword
     * @param radius Search radius
     * @param unit Distance unit (miles/km)
     * @param geoPoint Geohash location
     * @param segmentId Event segment ID (optional)
     * @return List of Event objects (returns JSON array directly)
     */
    @GET("api/manage/search")
    Call<java.util.List<Event>> searchEvents(
            @Query("keyword") String keyword,
            @Query("radius") String radius,
            @Query("unit") String unit,
            @Query("geoPoint") String geoPoint,
            @Query("segmentId") String segmentId
    );
    
    /**
     * Get event details by ID
     * @param eventId Event ID
     * @return EventDetails object
     */
    @GET("api/manage/event")
    Call<EventDetails> getEventDetails(@Query("id") String eventId);
    
    /**
     * Get artist/team information
     * @param artistName Artist or team name
     * @return Artist object with details
     */
    @GET("api/manage/artist")
    Call<Artist> getArtistInfo(@Query("name") String artistName);
    
    /**
     * Get venue details
     * @param venueName Venue name
     * @return Venue object with details
     */
    @GET("api/manage/venue")
    Call<com.example.myapplication.models.Venue> getVenueInfo(@Query("name") String venueName);
    
    /**
     * Get autocomplete suggestions
     * @param keyword Partial keyword
     * @return List of suggestions
     */
    @GET("api/manage/autocomplete")
    Call<java.util.List<String>> getAutocompleteSuggestions(@Query("keyword") String keyword);
    
    /**
     * Get event suggestions for autocomplete
     * @param keyword Partial keyword
     * @return SuggestResponse with embedded events
     */
    @GET("api/manage/suggest")
    Call<com.example.myapplication.api.responses.SuggestResponse> getSuggestions(@Query("keyword") String keyword);
    
    /**
     * Geocode an address to get coordinates
     * @param address Address to geocode
     * @return GeocodeResponse with coordinates
     */
    @GET("api/manage/geocode")
    Call<com.example.myapplication.api.responses.GeocodeResponse> geocodeAddress(@Query("address") String address);
    
    /**
     * Get IP info for current location
     * @return IpInfoResponse with location data
     */
    @GET("api/manage/ipinfo")
    Call<com.example.myapplication.api.responses.IpInfoResponse> getIpInfo();
    
    /**
     * Get Spotify artist information
     * @param artistName Artist name
     * @return SpotifyArtist object with artist details
     */
    @GET("api/manage/spotify/artist")
    Call<com.example.myapplication.models.SpotifyArtist> getSpotifyArtist(@Query("name") String artistName);
    
    /**
     * Get Spotify albums for an artist
     * @param artistName Artist name
     * @return List of SpotifyAlbum objects
     */
    @GET("api/manage/spotify/albums")
    Call<java.util.List<com.example.myapplication.models.SpotifyAlbum>> getSpotifyAlbums(@Query("name") String artistName);
    
    /**
     * Get all favorite events from backend
     * @return List of favorite Event objects (returns JSON array directly)
     */
    @GET("api/manage/favorites")
    Call<java.util.List<Event>> getFavorites();
    
    /**
     * Add event to favorites
     * @param body Request body with event and event_id
     * @return Response
     */
    @POST("api/manage/favorites")
    Call<Void> addFavorite(@retrofit2.http.Body FavoriteRequest body);
    
    /**
     * Remove event from favorites
     * @param eventId Event ID to remove
     * @return Response
     */
    @retrofit2.http.DELETE("api/manage/favorites")
    Call<Void> removeFavorite(@Query("event_id") String eventId);
    
    // Request body class for favorites
    class FavoriteRequest {
        @SerializedName("event")
        public Event event;
        
        @SerializedName("event_id")
        public String eventId;
        
        public FavoriteRequest(Event event, String eventId) {
            this.event = event;
            this.eventId = eventId;
        }
    }
}

