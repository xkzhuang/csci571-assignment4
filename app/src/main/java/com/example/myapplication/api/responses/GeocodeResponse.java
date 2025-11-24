package com.example.myapplication.api.responses;

import com.google.gson.annotations.SerializedName;

public class GeocodeResponse {
    @SerializedName("lat")
    private double latitude;

    @SerializedName("lng")
    private double longitude;

    @SerializedName("geohash")
    private String geohash;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getGeohash() {
        return geohash;
    }
}

