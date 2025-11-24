package com.example.myapplication.api.responses;

import com.example.myapplication.models.Event;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SuggestResponse {
    @SerializedName("_embedded")
    private Embedded embedded;

    public List<Event> getEvents() {
        return embedded != null ? embedded.events : null;
    }

    public static class Embedded {
        @SerializedName("attractions")
        public List<Event> events;
    }
}

