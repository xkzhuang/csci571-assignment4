package com.example.myapplication.utils;

import ch.hsr.geohash.GeoHash;

public class GeohashUtil {
    
    /**
     * Convert latitude and longitude to geohash
     * @param latitude Latitude
     * @param longitude Longitude
     * @return Geohash string
     */
    public static String encode(double latitude, double longitude) {
        return encode(latitude, longitude, 7); // Default precision of 7
    }
    
    /**
     * Convert latitude and longitude to geohash with specified precision
     * @param latitude Latitude
     * @param longitude Longitude
     * @param precision Precision level
     * @return Geohash string
     */
    public static String encode(double latitude, double longitude, int precision) {
        GeoHash geoHash = GeoHash.withCharacterPrecision(latitude, longitude, precision);
        return geoHash.toBase32();
    }
}

