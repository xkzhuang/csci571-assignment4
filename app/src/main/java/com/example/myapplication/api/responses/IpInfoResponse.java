package com.example.myapplication.api.responses;

import com.google.gson.annotations.SerializedName;

public class IpInfoResponse {
    @SerializedName("city")
    private String city;

    @SerializedName("region")
    private String region;

    @SerializedName("country")
    private String country;

    @SerializedName("loc")
    private String location; // Format: "lat,lng"

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

    public String getFormattedAddress() {
        return city + ", " + region + ", " + country;
    }
}

