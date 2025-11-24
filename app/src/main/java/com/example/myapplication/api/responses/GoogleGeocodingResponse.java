package com.example.myapplication.api.responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response from Google Geocoding API
 */
public class GoogleGeocodingResponse {
    @SerializedName("results")
    private List<Result> results;

    @SerializedName("status")
    private String status;

    public List<Result> getResults() {
        return results;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Check if geocoding was successful
     * @return true if successful
     */
    public boolean isSuccessful() {
        return "OK".equals(status) && results != null && !results.isEmpty();
    }

    /**
     * Get the first result's latitude
     * @return latitude or 0.0 if not available
     */
    public double getLatitude() {
        if (isSuccessful() && results.get(0) != null) {
            Result result = results.get(0);
            if (result.getGeometry() != null && result.getGeometry().getLocation() != null) {
                return result.getGeometry().getLocation().getLat();
            }
        }
        return 0.0;
    }

    /**
     * Get the first result's longitude
     * @return longitude or 0.0 if not available
     */
    public double getLongitude() {
        if (isSuccessful() && results.get(0) != null) {
            Result result = results.get(0);
            if (result.getGeometry() != null && result.getGeometry().getLocation() != null) {
                return result.getGeometry().getLocation().getLng();
            }
        }
        return 0.0;
    }

    public static class Result {
        @SerializedName("formatted_address")
        private String formattedAddress;

        @SerializedName("geometry")
        private Geometry geometry;

        @SerializedName("place_id")
        private String placeId;

        public String getFormattedAddress() {
            return formattedAddress;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public String getPlaceId() {
            return placeId;
        }
    }

    public static class Geometry {
        @SerializedName("location")
        private Location location;

        @SerializedName("location_type")
        private String locationType;

        public Location getLocation() {
            return location;
        }

        public String getLocationType() {
            return locationType;
        }
    }

    public static class Location {
        @SerializedName("lat")
        private double lat;

        @SerializedName("lng")
        private double lng;

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }
    }
}

