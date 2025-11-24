package com.example.myapplication.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.EventDetailsActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapters.EventsAdapter;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.models.Event;
import com.example.myapplication.utils.CategoryMapper;
import com.example.myapplication.utils.GeohashUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment implements EventsAdapter.OnEventClickListener {
    
    private static final String TAG = "SearchFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private TextInputEditText keywordEditText;
    private AutoCompleteTextView categorySpinner;
    private TextInputEditText locationEditText;
    private SwitchMaterial autoDetectSwitch;
    private TextInputEditText distanceEditText;
    private MaterialButton searchButton;
    private RecyclerView resultsRecyclerView;
    private TextView noResultsTextView;
    private ProgressBar loadingProgressBar;
    
    private EventsAdapter eventsAdapter;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<Intent> eventDetailsLauncher;
    private List<Event> currentEvents = new ArrayList<>();
    
    private String currentGeohash = "";
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup activity result launcher for EventDetailsActivity
        eventDetailsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                        Event updatedEvent = (Event) result.getData().getSerializableExtra("updated_event");
                        if (updatedEvent != null) {
                            // Update the event in current events list
                            updateEventInList(updatedEvent);
                            // Refresh adapter
                            if (eventsAdapter != null) {
                                eventsAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        keywordEditText = view.findViewById(R.id.keywordEditText);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        locationEditText = view.findViewById(R.id.locationEditText);
        autoDetectSwitch = view.findViewById(R.id.autoDetectSwitch);
        distanceEditText = view.findViewById(R.id.distanceEditText);
        searchButton = view.findViewById(R.id.searchButton);
        resultsRecyclerView = view.findViewById(R.id.resultsRecyclerView);
        noResultsTextView = view.findViewById(R.id.noResultsTextView);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        
        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        
        // Setup category spinner
        setupCategorySpinner();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup listeners
        setupListeners();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Note: Removed notifyDataSetChanged() call for performance
        // The adapter is already updated via ActivityResultLauncher callback
        // Calling notifyDataSetChanged() here causes unnecessary full redraws
    }
    
    private void setupCategorySpinner() {
        String[] categories = {
                getString(R.string.category_all),
                getString(R.string.category_music),
                getString(R.string.category_sports),
                getString(R.string.category_arts_theatre),
                getString(R.string.category_film),
                getString(R.string.category_miscellaneous)
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        categorySpinner.setAdapter(adapter);
    }
    
    private void setupRecyclerView() {
        eventsAdapter = new EventsAdapter(requireContext(), this);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        resultsRecyclerView.setAdapter(eventsAdapter);
    }
    
    private void setupListeners() {
        // Auto-detect location switch
        autoDetectSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            locationEditText.setEnabled(!isChecked);
            if (isChecked) {
                requestLocationPermissionAndDetect();
            } else {
                locationEditText.setText("");
                currentGeohash = "";
            }
        });
        
        // Search button
        searchButton.setOnClickListener(v -> performSearch());
    }
    
    private void requestLocationPermissionAndDetect() {
        if (ActivityCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            detectLocation();
        }
    }
    
    private void detectLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        currentGeohash = GeohashUtil.encode(location.getLatitude(), location.getLongitude());
                        locationEditText.setText("Current Location");
                    } else {
                        Toast.makeText(requireContext(), 
                                R.string.location_unavailable, Toast.LENGTH_SHORT).show();
                        autoDetectSwitch.setChecked(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), 
                            R.string.location_unavailable, Toast.LENGTH_SHORT).show();
                    autoDetectSwitch.setChecked(false);
                });
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                detectLocation();
            } else {
                Toast.makeText(requireContext(), 
                        R.string.location_permission_required, Toast.LENGTH_SHORT).show();
                autoDetectSwitch.setChecked(false);
            }
        }
    }
    
    private void performSearch() {
        String keyword = keywordEditText.getText().toString().trim();
        String category = categorySpinner.getText().toString().trim();
        String distance = distanceEditText.getText().toString().trim();
        
        // Validate inputs
        if (keyword.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a keyword", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (distance.isEmpty()) {
            distance = "10";
        }
        
        // Get location/geohash
        String geoPoint = currentGeohash;
        if (geoPoint.isEmpty() && !autoDetectSwitch.isChecked()) {
            String locationText = locationEditText.getText().toString().trim();
            if (!locationText.isEmpty()) {
                // In a real app, you'd geocode the location text to geohash
                // For now, we'll use a default geohash
                geoPoint = "9q5ctrk7"; // Default Los Angeles geohash
            }
        }
        
        if (geoPoint.isEmpty()) {
            Toast.makeText(requireContext(), "Please provide a location", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Convert category to segmentId
        String segmentId = CategoryMapper.getSegmentId(category);
        
        // Show loading
        showLoading(true);
        
        // Make API call
        Log.d(TAG, "Searching events: keyword=" + keyword + ", distance=" + distance + 
                ", geoPoint=" + geoPoint + ", segmentId=" + segmentId);
        
        ApiClient.getApiService().searchEvents(keyword, distance, "miles", geoPoint, segmentId)
                .enqueue(new Callback<List<Event>>() {
                    @Override
                    public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                        showLoading(false);
                        
                        Log.d(TAG, "Search Response Code: " + response.code());
                        Log.d(TAG, "Request URL: " + call.request().url());
                        
                        if (response.isSuccessful()) {
                            List<Event> events = response.body();
                            Log.d(TAG, "Events received: " + (events != null ? events.size() : "null"));
                            
                            if (events != null && !events.isEmpty()) {
                                // Log first event details for debugging
                                Event firstEvent = events.get(0);
                                if (firstEvent != null) {
                                    Log.d(TAG, "First event ID: " + (firstEvent.getId() != null ? firstEvent.getId() : "null"));
                                    Log.d(TAG, "First event name: " + (firstEvent.getName() != null ? firstEvent.getName() : "null"));
                                    Log.d(TAG, "First event image: " + firstEvent.getImageUrl());
                                    Log.d(TAG, "First event category: " + firstEvent.getCategory());
                                    Log.d(TAG, "First event dateTime: " + firstEvent.getDateTime());
                                    Log.d(TAG, "First event venue: " + firstEvent.getVenue());
                                }
                                
                                currentEvents = new ArrayList<>(events);
                                eventsAdapter.setEvents(events);
                                resultsRecyclerView.setVisibility(View.VISIBLE);
                                noResultsTextView.setVisibility(View.GONE);
                            } else {
                                showNoResults();
                            }
                        } else {
                            Log.e(TAG, "Search failed with code: " + response.code());
                            try {
                                String errorBody = response.errorBody() != null ? 
                                        response.errorBody().string() : "No error body";
                                Log.e(TAG, "Error Body: " + errorBody);
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body", e);
                            }
                            Toast.makeText(requireContext(), 
                                    "Error: " + response.code() + " - " + response.message(), 
                                    Toast.LENGTH_LONG).show();
                            showError();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Event>> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "=== Search FAILED ===");
                        Log.e(TAG, "Error type: " + t.getClass().getName());
                        Log.e(TAG, "Error message: " + t.getMessage());
                        Log.e(TAG, "Full stack trace:", t);
                        
                        String errorMsg = "Search failed: " + t.getMessage();
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                        showError();
                    }
                });
    }
    
    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        resultsRecyclerView.setVisibility(View.GONE);
        noResultsTextView.setVisibility(View.GONE);
    }
    
    private void showNoResults() {
        resultsRecyclerView.setVisibility(View.GONE);
        noResultsTextView.setVisibility(View.VISIBLE);
        noResultsTextView.setText(R.string.no_results);
    }
    
    private void showError() {
        resultsRecyclerView.setVisibility(View.GONE);
        noResultsTextView.setVisibility(View.VISIBLE);
        noResultsTextView.setText(R.string.error_occurred);
    }
    
    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(requireContext(), EventDetailsActivity.class);
        intent.putExtra("event", event);
        eventDetailsLauncher.launch(intent);
    }
    
    private void updateEventInList(Event updatedEvent) {
        // Find and update the event in current events list
        for (int i = 0; i < currentEvents.size(); i++) {
            Event event = currentEvents.get(i);
            if (event.getId().equals(updatedEvent.getId())) {
                event.setFavorited(updatedEvent.isFavorited());
                Log.d(TAG, "Updated event favorite status: " + event.getName() + " -> " + event.isFavorited());
                break;
            }
        }
    }
    
    @Override
    public void onFavoriteClick(Event event) {
        // The adapter has already toggled the isFavorited state for responsive UI
        // Now make the appropriate API call based on the new state
        if (event.isFavorited()) {
            // Add to favorites - POST request
            ApiService.FavoriteRequest request = new ApiService.FavoriteRequest(event, event.getId());
            ApiClient.getApiService().addFavorite(request)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                String message = event.getName() + " added to favorites!";
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Event added to favorites: " + event.getName());
                            } else {
                                // Revert the state if API call failed
                                event.setFavorited(false);
                                eventsAdapter.notifyDataSetChanged();
                                Toast.makeText(requireContext(), 
                                        "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Failed to add favorite. Response code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            // Revert the state if API call failed
                            event.setFavorited(false);
                            eventsAdapter.notifyDataSetChanged();
                            Log.e(TAG, "Error adding to favorites", t);
                            Toast.makeText(requireContext(), 
                                    "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Remove from favorites - DELETE request
            ApiClient.getApiService().removeFavorite(event.getId())
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                String message = event.getName() + " removed from favorites";
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Event removed from favorites: " + event.getName());
                            } else {
                                // Revert the state if API call failed
                                event.setFavorited(true);
                                eventsAdapter.notifyDataSetChanged();
                                Toast.makeText(requireContext(), 
                                        "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Failed to remove favorite. Response code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            // Revert the state if API call failed
                            event.setFavorited(true);
                            eventsAdapter.notifyDataSetChanged();
                            Log.e(TAG, "Error removing from favorites", t);
                            Toast.makeText(requireContext(), 
                                    "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}

