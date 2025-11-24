package com.example.myapplication.api.responses;

import com.example.myapplication.models.Event;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchResponse {
    @SerializedName("events")
    private List<Event> events;
    
    @SerializedName("total")
    private int total;
    
    @SerializedName("page")
    private int page;
    
    public SearchResponse() {}
    
    public List<Event> getEvents() {
        return events;
    }
    
    public void setEvents(List<Event> events) {
        this.events = events;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
}

