package com.example.myapplication.api.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Response from ipinfo.io API
 */
public class IpInfoDirectResponse {
    @SerializedName("ip")
    private String ip;

    @SerializedName("city")
    private String city;

    @SerializedName("region")
    private String region;

    @SerializedName("country")
    private String country;

    @SerializedName("loc")
    private String location; // Format: "lat,lng"

    @SerializedName("org")
    private String organization;

    @SerializedName("postal")
    private String postal;

    @SerializedName("timezone")
    private String timezone;

    public String getIp() {
        return ip;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getCountry() {
        return country;
    }

    public String getLocation() {
        return location;
    }

    public String getOrganization() {
        return organization;
    }

    public String getPostal() {
        return postal;
    }

    public String getTimezone() {
        return timezone;
    }

    /**
     * Get latitude from location string
     * @return latitude
     */
    public double getLatitude() {
        if (location != null && location.contains(",")) {
            String[] parts = location.split(",");
            try {
                return Double.parseDouble(parts[0]);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * Get longitude from location string
     * @return longitude
     */
    public double getLongitude() {
        if (location != null && location.contains(",")) {
            String[] parts = location.split(",");
            try {
                return Double.parseDouble(parts[1]);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * Get formatted address
     * @return formatted address string
     */
    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (city != null && !city.isEmpty()) {
            sb.append(city);
        }
        if (region != null && !region.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(region);
        }
        if (country != null && !country.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(country);
        }
        return sb.toString();
    }
}

