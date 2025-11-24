package com.example.myapplication.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Event;
import com.example.myapplication.models.EventDetails;
import com.example.myapplication.utils.DateFormatter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashSet;
import java.util.Locale;

public class InfoTabFragment extends Fragment {
    
    private static final String TAG = "InfoTabFragment";
    
    private TextView dateTextView;
    private TextView artistTextView;
    private TextView venueTextView;
    private TextView genresLabelTextView;
    private ChipGroup genresChipGroup;
    private Chip ticketStatusChip;
    private ImageButton externalLinkButton;
    private ImageButton shareButton;
    private MaterialCardView seatmapCard;
    private ImageView seatmapImageView;
    
    private Event event;
    private EventDetails eventDetails;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details_tab, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get event and eventDetails from arguments
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
            eventDetails = (EventDetails) getArguments().getSerializable("eventDetails");
        }
        
        if (event == null) {
            return;
        }
        
        // Initialize views
        dateTextView = view.findViewById(R.id.dateTextView);
        artistTextView = view.findViewById(R.id.artistTextView);
        venueTextView = view.findViewById(R.id.venueTextView);
        genresLabelTextView = view.findViewById(R.id.genresLabelTextView);
        genresChipGroup = view.findViewById(R.id.genresChipGroup);
        ticketStatusChip = view.findViewById(R.id.ticketStatusChip);
        externalLinkButton = view.findViewById(R.id.externalLinkButton);
        shareButton = view.findViewById(R.id.shareButton);
        seatmapCard = view.findViewById(R.id.seatmapCard);
        seatmapImageView = view.findViewById(R.id.seatmapImageView);
        
        // Setup UI
        setupEventInfo();
        setupActionButtons();
    }
    
    private void setupEventInfo() {
        // Use EventDetails if available, otherwise fall back to Event
        
        // Date
        String dateTime = eventDetails != null ? eventDetails.getDateTime() : event.getDateTime();
        String formattedDate = DateFormatter.formatEventDate(dateTime);
        dateTextView.setText(formattedDate);
        
        // Artist/Team
        String artists = eventDetails != null ? eventDetails.getArtistsString() : event.getCategory();
        artistTextView.setText(artists != null && !artists.isEmpty() ? artists : "N/A");
        
        // Venue
        String venueName = eventDetails != null ? eventDetails.getVenueName() : event.getVenue();
        venueTextView.setText(venueName != null && !venueName.isEmpty() ? venueName : "N/A");
        
        // Genres - populate chips dynamically from classifications
        setupGenreChips();
        
        // Ticket Status
        String statusCode = eventDetails != null ? eventDetails.getTicketStatusCode() : "onsale";
        Log.d(TAG, "Ticket status code: " + statusCode);
        updateTicketStatus(statusCode);
        
        // Seatmap
        if (eventDetails != null && eventDetails.getSeatmapUrl() != null && !eventDetails.getSeatmapUrl().isEmpty()) {
            Log.d(TAG, "Loading seatmap: " + eventDetails.getSeatmapUrl());
            seatmapCard.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(eventDetails.getSeatmapUrl())
                    .fitCenter()
                    .into(seatmapImageView);
        } else {
            Log.d(TAG, "No seatmap available");
            seatmapCard.setVisibility(View.GONE);
        }
    }
    
    private void setupGenreChips() {
        if (genresChipGroup == null || genresLabelTextView == null) {
            return;
        }
        
        genresChipGroup.removeAllViews();
        
        // Get classifications from eventDetails or event
        java.util.List<Event.Classification> classifications = 
                eventDetails != null ? eventDetails.getClassifications() : 
                (event != null ? event.getClassifications() : null);
        
        if (classifications == null || classifications.isEmpty()) {
            // Hide genres label if no classifications
            genresLabelTextView.setVisibility(View.GONE);
            return;
        }
        
        // Get the first classification - safe access
        Event.Classification classification = classifications.get(0);
        if (classification == null) {
            genresLabelTextView.setVisibility(View.GONE);
            return;
        }
        
        // Add chips in order: segment, genre, subGenre, type, subType
        HashSet<String> hasSeen = new HashSet<String>();
        addChipIfValid(classification.getSegment() != null ? classification.getSegment().getName() : null, hasSeen);
        addChipIfValid(classification.getGenre() != null ? classification.getGenre().getName() : null, hasSeen);
        addChipIfValid(classification.getSubGenre() != null ? classification.getSubGenre().getName() : null, hasSeen);
        addChipIfValid(classification.getType() != null ? classification.getType().getName() : null, hasSeen);
        addChipIfValid(classification.getSubType() != null ? classification.getSubType().getName() : null, hasSeen);
        
        // Hide genres label if no chips were added
        if (genresChipGroup.getChildCount() == 0) {
            genresLabelTextView.setVisibility(View.GONE);
        } else {
            genresLabelTextView.setVisibility(View.VISIBLE);
        }
    }
    
    private void addChipIfValid(String text, HashSet<String> hasSeen) {
        if (text == null || text.isEmpty() || 
            text.equalsIgnoreCase("Unknown") || 
            text.equalsIgnoreCase("Undefined") ||
            !hasSeen.add(text)) {
            return;
        }
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setClickable(false);
        chip.setCheckable(false);
        genresChipGroup.addView(chip);
    }
    
    private void updateTicketStatus(String statusCode) {
        if (statusCode == null) {
            statusCode = "onsale";
        }
        
        String normalizedStatus = statusCode.toLowerCase(Locale.US);
        
        int backgroundAttr = com.google.android.material.R.attr.colorPrimary;
        int textAttr = com.google.android.material.R.attr.colorOnPrimary;
        int labelRes = R.string.on_sale;
        
        switch (normalizedStatus) {
            case "onsale":
                labelRes = R.string.on_sale;
                backgroundAttr = com.google.android.material.R.attr.colorPrimary;
                textAttr = com.google.android.material.R.attr.colorOnPrimary;
                break;
            case "offsale":
                labelRes = R.string.off_sale;
                backgroundAttr = com.google.android.material.R.attr.colorSecondary;
                textAttr = com.google.android.material.R.attr.colorOnSecondary;
                break;
            case "canceled":
            case "cancelled":
                labelRes = R.string.canceled;
                backgroundAttr = com.google.android.material.R.attr.colorError;
                textAttr = com.google.android.material.R.attr.colorOnError;
                break;
            case "postponed":
            case "rescheduled":
                labelRes = R.string.postponed;
                backgroundAttr = com.google.android.material.R.attr.colorTertiary;
                textAttr = com.google.android.material.R.attr.colorOnTertiary;
                break;
            case "scheduled":
                labelRes = R.string.scheduled;
                backgroundAttr = com.google.android.material.R.attr.colorTertiary;
                textAttr = com.google.android.material.R.attr.colorOnTertiary;
                break;
            default:
                labelRes = R.string.on_sale;
                backgroundAttr = com.google.android.material.R.attr.colorPrimary;
                textAttr = com.google.android.material.R.attr.colorOnPrimary;
                break;
        }
        
        ticketStatusChip.setText(labelRes);
        int backgroundColor = resolveThemeColor(backgroundAttr);
        int textColor = resolveThemeColor(textAttr);
        ticketStatusChip.setChipBackgroundColor(ColorStateList.valueOf(backgroundColor));
        ticketStatusChip.setTextColor(textColor);
    }
    
    private int resolveThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }
    
    private void setupActionButtons() {
        // Check if URL exists in JSON (not including fallback Ticketmaster URL)
        String urlFromJson = getUrlFromJson();
        
        if (urlFromJson == null || urlFromJson.isEmpty()) {
            // Hide both buttons if no URL found in JSON
            externalLinkButton.setVisibility(View.GONE);
            shareButton.setVisibility(View.GONE);
        } else {
            // Show buttons and set up click listeners
            externalLinkButton.setVisibility(View.VISIBLE);
            shareButton.setVisibility(View.VISIBLE);
            
            // External link button - opens ticketing URL in browser
            externalLinkButton.setOnClickListener(v -> openTicketingUrl());
            
            // Share button - opens Android's native share dialog
            shareButton.setOnClickListener(v -> shareEvent());
        }
    }
    
    private String getUrlFromJson() {
        // Try to get ticketing URL from eventDetails
        String ticketingUrl = null;
        if (eventDetails != null) {
            ticketingUrl = eventDetails.getTicketingUrl();
        }
        
        // Fallback to event URL if no ticketing URL
        if (ticketingUrl == null || ticketingUrl.isEmpty()) {
            ticketingUrl = event != null ? event.getUrl() : null;
        }
        
        // Return only if URL exists in JSON (don't use fallback Ticketmaster URL)
        return ticketingUrl;
    }
    
    private void openTicketingUrl() {
        String ticketingUrl = getEventUrl();
        
        if (ticketingUrl != null && !ticketingUrl.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ticketingUrl));
            startActivity(intent);
        }
    }
    
    private String getEventUrl() {
        // Try to get ticketing URL from eventDetails
        String ticketingUrl = null;
        if (eventDetails != null) {
            ticketingUrl = eventDetails.getTicketingUrl();
        }
        
        // Fallback to event URL if no ticketing URL
        if (ticketingUrl == null || ticketingUrl.isEmpty()) {
            ticketingUrl = event != null ? event.getUrl() : null;
        }
        
        // Fallback to Ticketmaster URL using event ID if still no URL
        if ((ticketingUrl == null || ticketingUrl.isEmpty()) && event != null && event.getId() != null) {
            ticketingUrl = "https://www.ticketmaster.com/event/" + event.getId();
        }
        
        return ticketingUrl;
    }
    
    private void shareEvent() {
        String eventName = eventDetails != null && eventDetails.getName() != null ? 
                eventDetails.getName() : (event != null ? event.getName() : "Event");
        String venueName = eventDetails != null ? eventDetails.getVenueName() : 
                (event != null ? event.getVenue() : "");
        String url = getEventUrl();
        
        String shareText = "Check out " + eventName;
        if (venueName != null && !venueName.isEmpty()) {
            shareText += " at " + venueName;
        }
        if (url != null && !url.isEmpty()) {
            shareText += "\n" + url;
        }
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, eventName);
        
        startActivity(Intent.createChooser(shareIntent, "Share Event"));
    }
}

