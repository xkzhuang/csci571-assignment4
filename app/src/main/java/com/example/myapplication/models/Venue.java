package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Venue implements Serializable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("url")
    private String url;
    
    @SerializedName("address")
    private Address address;
    
    @SerializedName("city")
    private City city;
    
    @SerializedName("state")
    private State state;
    
    @SerializedName("country")
    private Country country;
    
    @SerializedName("postalCode")
    private String postalCode;
    
    @SerializedName("images")
    private List<Event.Image> images;
    
    // Nested classes for complex fields
    public static class Address implements Serializable {
        @SerializedName("line1")
        private String line1;
        
        public String getLine1() {
            return line1;
        }
    }
    
    public static class City implements Serializable {
        @SerializedName("name")
        private String name;
        
        public String getName() {
            return name;
        }
    }
    
    public static class State implements Serializable {
        @SerializedName("name")
        private String name;
        
        @SerializedName("stateCode")
        private String stateCode;
        
        public String getName() {
            return name;
        }
        
        public String getStateCode() {
            return stateCode;
        }
    }
    
    public static class Country implements Serializable {
        @SerializedName("name")
        private String name;
        
        @SerializedName("countryCode")
        private String countryCode;
        
        public String getName() {
            return name;
        }
        
        public String getCountryCode() {
            return countryCode;
        }
    }
    
    @SerializedName("latitude")
    private double latitude;
    
    @SerializedName("longitude")
    private double longitude;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("openHours")
    private String openHours;
    
    @SerializedName("generalRule")
    private String generalRule;
    
    @SerializedName("childRule")
    private String childRule;
    
    @SerializedName("image")
    private String imageUrl;
    
    // Constructors
    public Venue() {}
    
    public Venue(String name) {
        this.name = name;
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
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Address getAddressObject() {
        return address;
    }
    
    public void setAddressObject(Address address) {
        this.address = address;
    }
    
    public String getAddress() {
        return address != null && address.getLine1() != null ? address.getLine1() : "";
    }
    
    public City getCityObject() {
        return city;
    }
    
    public void setCityObject(City city) {
        this.city = city;
    }
    
    public String getCity() {
        return city != null && city.getName() != null ? city.getName() : "";
    }
    
    public State getStateObject() {
        return state;
    }
    
    public void setStateObject(State state) {
        this.state = state;
    }
    
    public String getState() {
        return state != null && state.getName() != null ? state.getName() : "";
    }
    
    public String getStateCode() {
        return state != null && state.getStateCode() != null ? state.getStateCode() : "";
    }
    
    public Country getCountryObject() {
        return country;
    }
    
    public void setCountryObject(Country country) {
        this.country = country;
    }
    
    public String getCountry() {
        return country != null && country.getName() != null ? country.getName() : "";
    }
    
    public String getCountryCode() {
        return country != null && country.getCountryCode() != null ? country.getCountryCode() : "";
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public List<Event.Image> getImages() {
        return images;
    }
    
    public void setImages(List<Event.Image> images) {
        this.images = images;
    }
    
    public String getImageUrlFromImages() {
        if (images != null && !images.isEmpty() && images.get(0) != null) {
            return images.get(0).getUrl();
        }
        return imageUrl; // fallback to legacy imageUrl field
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getOpenHours() {
        return openHours;
    }
    
    public void setOpenHours(String openHours) {
        this.openHours = openHours;
    }
    
    public String getGeneralRule() {
        return generalRule;
    }
    
    public void setGeneralRule(String generalRule) {
        this.generalRule = generalRule;
    }
    
    public String getChildRule() {
        return childRule;
    }
    
    public void setChildRule(String childRule) {
        this.childRule = childRule;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        String addressStr = getAddress();
        String cityStr = getCity();
        String stateStr = getState();
        
        if (addressStr != null && !addressStr.isEmpty()) {
            sb.append(addressStr);
        }
        if (cityStr != null && !cityStr.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(cityStr);
        }
        if (stateStr != null && !stateStr.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(stateStr);
        }
        if (postalCode != null && !postalCode.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(postalCode);
        }
        return sb.toString();
    }
    
    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        String addressStr = getAddress();
        String cityStr = getCity();
        String stateCodeStr = getStateCode();
        String countryCodeStr = getCountryCode();
        
        if (addressStr != null && !addressStr.isEmpty()) {
            sb.append(addressStr);
        }
        if (cityStr != null && !cityStr.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(cityStr);
        }
        if (stateCodeStr != null && !stateCodeStr.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(stateCodeStr);
        }
        if (countryCodeStr != null && !countryCodeStr.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(countryCodeStr);
        }
        return sb.toString();
    }
}

