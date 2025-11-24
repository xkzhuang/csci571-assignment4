package com.example.myapplication.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Event;
import com.example.myapplication.models.EventDetails;
import com.example.myapplication.models.Venue;

public class VenueTabFragment extends Fragment {
    
    private ImageView venueImageView;
    private TextView venueNameTextView;
    private TextView addressTextView;
    private ImageButton externalLinkButton;
    
    private Event event;
    private EventDetails eventDetails;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_venue_tab, container, false);
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
        venueImageView = view.findViewById(R.id.venueImageView);
        venueNameTextView = view.findViewById(R.id.venueNameTextView);
        addressTextView = view.findViewById(R.id.addressTextView);
        externalLinkButton = view.findViewById(R.id.externalLinkButton);
        
        // Setup UI
        setupVenueInfo();
    }
    
    private void setupVenueInfo() {
        Venue venue = null;
        
        // Try to get venue from eventDetails first
        if (eventDetails != null && eventDetails.getVenue() != null) {
            venue = eventDetails.getVenue();
        }
        
        if (venue != null) {
            // Venue name
            venueNameTextView.setText(venue.getName());
            
            // Venue image
            String imageUrl = venue.getImageUrlFromImages();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                venueImageView.setVisibility(View.VISIBLE);
                Glide.with(requireContext())
                        .load(imageUrl)
                        .centerCrop()
                        .into(venueImageView);
            } else {
                venueImageView.setVisibility(View.GONE);
            }
            
            // Address (line1, city, stateCode, countryCode)
            String formattedAddress = venue.getFormattedAddress();
            if (formattedAddress != null && !formattedAddress.isEmpty()) {
                addressTextView.setText(formattedAddress);
            } else {
                addressTextView.setText("Address not available");
            }
            
            // External link button for venue's Ticketmaster page
            final String venueUrl = venue.getUrl();
            if (venueUrl != null && !venueUrl.isEmpty()) {
                externalLinkButton.setVisibility(View.VISIBLE);
                externalLinkButton.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(venueUrl));
                    startActivity(intent);
                });
            } else {
                externalLinkButton.setVisibility(View.GONE);
            }
        } else {
            // Fallback to event venue name
            venueNameTextView.setText(event.getVenue());
            addressTextView.setText("Address not available");
        }
    }
}

