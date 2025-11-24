package com.example.myapplication.utils;

public class CategoryMapper {
    
    /**
     * Get Ticketmaster segment ID based on category name
     * @param category Category name
     * @return Segment ID or null if category is "All" or not found
     */
    public static String getSegmentId(String category) {
        if (category == null || category.isEmpty()) {
            return null;
        }
        
        switch (category) {
            case "Music":
                return "KZFzniwnSyZfZ7v7nJ";
            case "Sports":
                return "KZFzniwnSyZfZ7v7nE";
            case "Arts & Theatre":
                return "KZFzniwnSyZfZ7v7na";
            case "Film":
                return "KZFzniwnSyZfZ7v7nn";
            case "Miscellaneous":
                return "KZFzniwnSyZfZ7v7n1";
            case "All":
            default:
                return null;
        }
    }
}

