package com.example.myapplication.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateFormatter {
    
    private static final String TAG = "DateFormatter";
    
    /**
     * Format date string to "MMM dd, yyyy, h:mm a" format
     * Handles both ISO 8601 with time and date-only formats
     * @param dateString Date string (e.g., "2026-03-08T02:00:00Z" or "2025-12-21")
     * @return Formatted date string (e.g., "Mar 08, 2026, 2:00 AM" or "Dec 21, 2025")
     */
    public static String formatEventDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }
        
        try {
            // Check if it's ISO 8601 with time (contains 'T')
            if (dateString.contains("T")) {
                // Parse ISO 8601 date with time
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = inputFormat.parse(dateString);
                
                if (date != null) {
                    // Format to desired output with time and year
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, h:mm a", Locale.US);
                    return outputFormat.format(date);
                }
            } else {
                // Parse date-only format (yyyy-MM-dd)
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = inputFormat.parse(dateString);
                
                if (date != null) {
                    // Format to desired output without time but with year
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                    return outputFormat.format(date);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date: " + dateString, e);
            // Return the original string if parsing fails
            return dateString;
        }
        
        return "";
    }
}

