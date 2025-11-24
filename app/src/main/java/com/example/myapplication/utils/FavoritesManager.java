package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.myapplication.models.Event;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {
    private static final String PREFS_NAME = "EventsAroundPrefs";
    private static final String KEY_FAVORITES = "favorites";
    
    private static FavoritesManager instance;
    private SharedPreferences preferences;
    private Gson gson;
    
    private FavoritesManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public static synchronized FavoritesManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoritesManager(context);
        }
        return instance;
    }
    
    /**
     * Add an event to favorites
     * @param event Event to add
     * @return true if added successfully, false if already exists
     */
    public boolean addFavorite(Event event) {
        if (event == null || event.getId() == null || event.getId().isEmpty()) {
            return false;
        }
        
        List<Event> favorites = getAllFavorites();
        
        // Check if event already exists
        for (Event fav : favorites) {
            if (fav != null && fav.getId() != null && fav.getId().equals(event.getId())) {
                return false; // Already exists
            }
        }
        
        favorites.add(event);
        saveFavorites(favorites);
        return true;
    }
    
    /**
     * Remove an event from favorites
     * @param eventId Event ID to remove
     * @return true if removed successfully, false if not found
     */
    public boolean removeFavorite(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return false;
        }
        
        List<Event> favorites = getAllFavorites();
        boolean removed = false;
        
        for (int i = 0; i < favorites.size(); i++) {
            Event event = favorites.get(i);
            if (event != null && event.getId() != null && event.getId().equals(eventId)) {
                favorites.remove(i);
                removed = true;
                break;
            }
        }
        
        if (removed) {
            saveFavorites(favorites);
        }
        
        return removed;
    }
    
    /**
     * Check if an event is in favorites
     * @param eventId Event ID to check
     * @return true if event is in favorites
     */
    public boolean isFavorite(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return false;
        }
        
        List<Event> favorites = getAllFavorites();
        for (Event event : favorites) {
            if (event != null && event.getId() != null && event.getId().equals(eventId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Toggle favorite status of an event
     * @param event Event to toggle
     * @return true if now favorited, false if unfavorited
     */
    public boolean toggleFavorite(Event event) {
        if (event == null || event.getId() == null || event.getId().isEmpty()) {
            return false;
        }
        
        if (isFavorite(event.getId())) {
            removeFavorite(event.getId());
            return false;
        } else {
            addFavorite(event);
            return true;
        }
    }
    
    /**
     * Get all favorite events
     * @return List of all favorite events
     */
    public List<Event> getAllFavorites() {
        String json = preferences.getString(KEY_FAVORITES, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<Event>>(){}.getType();
        List<Event> favorites = gson.fromJson(json, type);
        return favorites != null ? favorites : new ArrayList<>();
    }
    
    /**
     * Get the count of favorite events
     * @return Number of favorite events
     */
    public int getFavoritesCount() {
        return getAllFavorites().size();
    }
    
    /**
     * Clear all favorites
     */
    public void clearAllFavorites() {
        preferences.edit().remove(KEY_FAVORITES).apply();
    }
    
    /**
     * Save favorites list to SharedPreferences
     * @param favorites List of events to save
     */
    private void saveFavorites(List<Event> favorites) {
        String json = gson.toJson(favorites);
        preferences.edit().putString(KEY_FAVORITES, json).apply();
    }
}

