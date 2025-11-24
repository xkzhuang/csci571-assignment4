package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.EventsAdapter;
import com.example.myapplication.adapters.LocationAdapter;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.api.DirectApiClient;
import com.example.myapplication.api.GooglePlacesApiService;
import com.example.myapplication.api.requests.GooglePlacesAutocompleteRequest;
import com.example.myapplication.api.responses.GeocodeResponse;
import com.example.myapplication.api.responses.IpInfoResponse;
import com.example.myapplication.api.responses.IpInfoDirectResponse;
import com.example.myapplication.api.responses.GoogleGeocodingResponse;
import com.example.myapplication.api.responses.GooglePlacesV1AutocompleteResponse;
import com.example.myapplication.api.responses.SuggestResponse;
import com.example.myapplication.models.Event;
import com.example.myapplication.utils.CategoryMapper;
import com.example.myapplication.utils.GeohashUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity implements EventsAdapter.OnEventClickListener {

    private static final String TAG = "SearchActivity";
    private static final long AUTOCOMPLETE_DELAY_MS = 500; // Delay for autocomplete API calls (500ms)
    private static final long MIN_SEARCHING_DISPLAY_MS = 1500; // Minimum time to show "Searching..." (1.5 seconds)
    private static final String GOOGLE_API_KEY = "AIzaSyA5JegXZBFDWtqszH3Ny319CZXVISihU1Y";

    // UI Components
    private AutoCompleteTextView keywordEditText;
    private com.google.android.material.textfield.TextInputLayout keywordInputLayout;
    private AutoCompleteTextView locationSpinner;
    private com.google.android.material.textfield.TextInputLayout locationInputLayout;
    private TextInputEditText distanceEditText;
    private com.google.android.material.textfield.TextInputLayout distanceInputLayout;
    private TabLayout categoryTabLayout;
    private MaterialButton searchButton;
    private MaterialButton backButton;
    private RecyclerView resultsRecyclerView;
    private View noResultsTextView;
    private View loadingContainer;

    // Data
    private EventsAdapter eventsAdapter;
    private Handler autocompleteHandler = new Handler(Looper.getMainLooper());
    private Runnable autocompleteRunnable;
    private Handler locationSearchHandler = new Handler(Looper.getMainLooper());
    private Runnable locationSearchRunnable;
    private retrofit2.Call<GooglePlacesV1AutocompleteResponse> currentPlacesCall;
    private LocationAdapter locationAdapter;
    private List<String> pendingResults = null; // Store results until animation completes
    private String currentGeohash = "";
    private boolean isCustomLocation = false;
    private String selectedCategory = "All";
    private List<Event> allEvents = new ArrayList<>(); // Store all search results for client-side filtering
    private ActivityResultLauncher<Intent> eventDetailsLauncher;
    private boolean isItemSelected = false; // Flag to prevent dropdown from reopening after item selection
    private boolean isLocationItemSelected = false; // Flag to prevent API calls when location is selected
    private long searchingStartTime = 0; // Track when "Searching..." was shown

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        
        // Setup activity result launcher for EventDetailsActivity
        eventDetailsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Event updatedEvent = (Event) result.getData().getSerializableExtra("updated_event");
                        if (updatedEvent != null) {
                            // Update the event in allEvents list
                            updateEventInList(updatedEvent);
                            // Refresh adapter
                            eventsAdapter.notifyDataSetChanged();
                        }
                    }
                });
        
        setContentView(R.layout.activity_search);

        initializeViews();
        setupLocationSelector();
        setupKeywordAutocomplete();
        setupCategoryTabs();
        setupRecyclerView();
        setupListeners();
        
        // Request focus on keyword input field after layout is complete
        keywordEditText.post(() -> keywordEditText.requestFocus());
        
        // Auto-detect current location on start
        detectCurrentLocation();
    }

    private void initializeViews() {
        keywordEditText = findViewById(R.id.keywordEditText);
        keywordInputLayout = findViewById(R.id.keywordInputLayout);
        locationSpinner = findViewById(R.id.locationSpinner);
        locationInputLayout = findViewById(R.id.locationInputLayout);
        distanceEditText = findViewById(R.id.distanceEditText);
        distanceInputLayout = findViewById(R.id.distanceInputLayout);
        categoryTabLayout = findViewById(R.id.categoryTabLayout);
        searchButton = findViewById(R.id.searchButton);
        backButton = findViewById(R.id.backButton);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        noResultsTextView = findViewById(R.id.noResultsCard);
        loadingContainer = findViewById(R.id.loadingContainer);
    }

    private void setupLocationSelector() {
        // Initialize custom adapter with empty list
        List<String> emptyList = new ArrayList<>();
        locationAdapter = new LocationAdapter(this, emptyList);
        locationAdapter.setShowCurrentLocation(true);
        locationSpinner.setAdapter(locationAdapter);
        locationSpinner.setText("Current Location", false);

        // Handle focus changes
        locationSpinner.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // When focused, switch to custom location mode and clear text if it's "Current Location"
                String currentText = locationSpinner.getText().toString();
                if ("Current Location".equals(currentText)) {
                    locationSpinner.setText("");
                }
                // Immediately switch to custom location mode
                isCustomLocation = true;
                // Show dropdown with Current Location option
                locationSpinner.showDropDown();
            }
        });

        // Handle item selection from dropdown
        locationSpinner.setOnItemClickListener((parent, view, position, id) -> {
            // Set flag to prevent API call from text change listener
            isLocationItemSelected = true;
            
            // Cancel any pending API calls
            if (locationSearchRunnable != null) {
                locationSearchHandler.removeCallbacks(locationSearchRunnable);
                locationSearchRunnable = null;
            }
            
            // Cancel any in-flight API calls
            if (currentPlacesCall != null && !currentPlacesCall.isCanceled()) {
                currentPlacesCall.cancel();
                currentPlacesCall = null;
            }
            
            // Clear pending results
            pendingResults = null;
            
            // Hide "Searching..." indicator immediately
            locationAdapter.setShowSearching(false);
            searchingStartTime = 0;
            
            String selected = locationAdapter.getItem(position);
            if ("Current Location".equals(selected)) {
                // Only switch back to current location mode when explicitly selected
                isCustomLocation = false;
                locationSpinner.setText("Current Location", false);
                detectCurrentLocation();
            } else if (selected != null && !"Searching...".equals(selected)) {
                // User selected a search result
                locationSpinner.setText(selected, false);
                isCustomLocation = true;
            }
            
            // Close dropdown completely
            locationSpinner.post(() -> {
                locationSpinner.dismissDropDown();
                locationSpinner.clearFocus();
                // Reset flag after a short delay to allow text change to complete
                locationSearchHandler.postDelayed(() -> {
                    isLocationItemSelected = false;
                }, 100);
            });
        });

        // Handle text changes to trigger Google Places search
        locationSpinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Don't trigger API call if item was just selected
                if (isLocationItemSelected) {
                    return;
                }
                
                String text = s.toString().trim();
                
                // Clear error when user starts typing
                if (locationInputLayout.isErrorEnabled()) {
                    locationInputLayout.setErrorEnabled(false);
                    locationInputLayout.setError(null);
                }
                
                // Stay in custom location mode for any text that's not "Current Location"
                if (!"Current Location".equals(text)) {
                    isCustomLocation = true;
                }
                
                // Cancel previous search request
                if (locationSearchRunnable != null) {
                    locationSearchHandler.removeCallbacks(locationSearchRunnable);
                }
                
                // Show dropdown immediately when typing
                if (locationSpinner.hasFocus()) {
                    locationSpinner.showDropDown();
                }
                
                // If text is empty or "Current Location", just show Current Location option
                if (text.isEmpty() || "Current Location".equals(text)) {
                    locationAdapter.setShowSearching(false);
                    locationAdapter.updateSearchResults(new ArrayList<>());
                    searchingStartTime = 0;
                    return;
                }
                
                // Schedule new search request after user stops typing
                locationSearchRunnable = () -> {
                    // Show "Searching..." indicator and record start time
                    searchingStartTime = System.currentTimeMillis();
                    pendingResults = null; // Clear any previous results
                    
                    runOnUiThread(() -> {
                        locationAdapter.setShowSearching(true);
                        locationAdapter.updateSearchResults(new ArrayList<>());
                        // Force adapter refresh
                        locationSpinner.setAdapter(locationAdapter);
                        
                        // Always show dropdown when showing Searching
                        locationSpinner.post(() -> {
                            locationSpinner.showDropDown();
                        });
                    });
                    
                    // Start API call
                    searchGooglePlaces(text);
                    
                    // Schedule to hide "Searching..." and show results after 1.5 seconds
                    locationSearchHandler.postDelayed(() -> {
                        showResultsAndHideSearching();
                    }, MIN_SEARCHING_DISPLAY_MS);
                };
                locationSearchHandler.postDelayed(locationSearchRunnable, AUTOCOMPLETE_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Make dropdown show on click
        locationSpinner.setOnClickListener(v -> {
            locationSpinner.showDropDown();
        });
    }
    
    private void searchGooglePlaces(String input) {
        if (input == null || input.trim().isEmpty()) {
            pendingResults = new ArrayList<>();
            return;
        }
        
        final String trimmedInput = input.trim();
        
        // Create request with input and includedPrimaryTypes
        GooglePlacesAutocompleteRequest request = new GooglePlacesAutocompleteRequest(trimmedInput);
        String fullUrl = "https://places.googleapis.com/v1/places:autocomplete";
        
        // Cancel any previous in-flight call
        if (currentPlacesCall != null && !currentPlacesCall.isCanceled()) {
            currentPlacesCall.cancel();
        }
        
        currentPlacesCall = DirectApiClient.getGooglePlacesService().getPlacePredictions(fullUrl, GOOGLE_API_KEY, request);
        currentPlacesCall.enqueue(new Callback<GooglePlacesV1AutocompleteResponse>() {
                    @Override
                    public void onResponse(Call<GooglePlacesV1AutocompleteResponse> call, 
                                         Response<GooglePlacesV1AutocompleteResponse> response) {
                        // Clear the call reference
                        if (currentPlacesCall == call) {
                            currentPlacesCall = null;
                        }
                        
                        if (response.isSuccessful() && response.body() != null) {
                            GooglePlacesV1AutocompleteResponse placesResponse = response.body();
                            
                            if (placesResponse.getSuggestions() != null && !placesResponse.getSuggestions().isEmpty()) {
                                List<String> results = new ArrayList<>();
                                for (GooglePlacesV1AutocompleteResponse.Suggestion suggestion : placesResponse.getSuggestions()) {
                                    if (suggestion.getPlacePrediction() != null &&
                                        suggestion.getPlacePrediction().getText() != null &&
                                        suggestion.getPlacePrediction().getText().getText() != null) {
                                        String placeText = suggestion.getPlacePrediction().getText().getText();
                                        if (placeText != null && !placeText.isEmpty()) {
                                            results.add(placeText);
                                        }
                                    }
                                }
                                
                                // Store results - they will be shown after 1.5 seconds
                                runOnUiThread(() -> {
                                    pendingResults = results;
                                });
                            } else {
                                runOnUiThread(() -> {
                                    pendingResults = new ArrayList<>();
                                });
                            }
                        } else {
                            if (response != null && response.errorBody() != null) {
                                try {
                                    response.errorBody().string();
                                } catch (Exception e) {
                                    Log.e(TAG, "Failed to read error body: " + e.getMessage());
                                }
                            }
                            Log.e(TAG, "Places API request failed. Code: " + (response != null ? response.code() : "null response"));
                            runOnUiThread(() -> {
                                pendingResults = new ArrayList<>();
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<GooglePlacesV1AutocompleteResponse> call, Throwable t) {
                        // Clear the call reference
                        if (currentPlacesCall == call) {
                            currentPlacesCall = null;
                        }
                        
                        // Only log error if not canceled
                        if (t != null && !call.isCanceled()) {
                            Log.e(TAG, "Places API request failed: " + t.getMessage());
                        }
                        
                        runOnUiThread(() -> {
                            pendingResults = new ArrayList<>();
                        });
                    }
                });
    }
    
    /**
     * Show results in dropdown and hide "Searching..." indicator
     * Called after 1.5 seconds from when "Searching..." was shown
     */
    private void showResultsAndHideSearching() {
        locationAdapter.setShowSearching(false);
        searchingStartTime = 0;
        
        // Populate results if available
        if (pendingResults != null) {
            locationAdapter.updateSearchResults(pendingResults);
            locationSpinner.setAdapter(locationAdapter);
            pendingResults = null;
            
            // Show dropdown with results
            locationSpinner.post(() -> {
                if (locationSpinner.hasFocus()) {
                    locationSpinner.showDropDown();
                }
            });
        } else {
            // No results yet or empty results
            locationAdapter.updateSearchResults(new ArrayList<>());
            locationSpinner.setAdapter(locationAdapter);
            
            // If still has focus, keep dropdown open with just "Current Location"
            if (locationSpinner.hasFocus()) {
                locationSpinner.post(() -> {
                    locationSpinner.showDropDown();
                });
            }
        }
    }

    private void setupKeywordAutocomplete() {
        keywordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Don't trigger autocomplete if item was just selected
                if (isItemSelected) {
                    isItemSelected = false;
                    return;
                }
                
                // Clear error when user starts typing
                if (keywordInputLayout.isErrorEnabled()) {
                    keywordInputLayout.setErrorEnabled(false);
                    keywordInputLayout.setError(null);
                }
                
                // Cancel previous autocomplete request
                if (autocompleteRunnable != null) {
                    autocompleteHandler.removeCallbacks(autocompleteRunnable);
                }

                // Schedule new autocomplete request
                if (s.length() > 0) {
                    autocompleteRunnable = () -> fetchAutocompleteSuggestions(s.toString());
                    autocompleteHandler.postDelayed(autocompleteRunnable, AUTOCOMPLETE_DELAY_MS);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Auto-close dropdown when item is selected
        keywordEditText.setOnItemClickListener((parent, view, position, id) -> {
            // Set flag to prevent autocomplete from triggering
            isItemSelected = true;
            
            // Cancel any pending autocomplete requests
            if (autocompleteRunnable != null) {
                autocompleteHandler.removeCallbacks(autocompleteRunnable);
                autocompleteRunnable = null;
            }
            
            // Dismiss dropdown after item selection
            keywordEditText.dismissDropDown();
            
            // Clear focus to prevent dropdown from reopening
            keywordEditText.clearFocus();
        });
    }

    private void fetchAutocompleteSuggestions(String keyword) {
        // Don't show dropdown if item was just selected
        if (isItemSelected) {
            return;
        }
        
        ApiClient.getApiService().getSuggestions(keyword)
                .enqueue(new Callback<SuggestResponse>() {
                    @Override
                    public void onResponse(Call<SuggestResponse> call, Response<SuggestResponse> response) {
                        // Don't show dropdown if item was just selected
                        if (isItemSelected) {
                            return;
                        }
                        
                        if (response.isSuccessful() && response.body() != null) {
                            List<Event> events = response.body().getEvents();
                            if (events != null && !events.isEmpty()) {
                                List<String> suggestions = new ArrayList<>();
                                // Add user input as the first option
                                suggestions.add(keyword);
                                
                                for (Event event : events) {
                                    if (event.getName() != null && !event.getName().equals(keyword)) {
                                        suggestions.add(event.getName());
                                    }
                                }
                                
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        SearchActivity.this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        suggestions
                                );
                                keywordEditText.setAdapter(adapter);
                                
                                // Only show dropdown if the field has focus and item wasn't just selected
                                if (keywordEditText.hasFocus() && !isItemSelected) {
                                    keywordEditText.showDropDown();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SuggestResponse> call, Throwable t) {
                        Log.e(TAG, "Autocomplete failed: " + t.getMessage());
                    }
                });
    }

    private void setupCategoryTabs() {
        String[] categories = {
                getString(R.string.category_all),
                getString(R.string.category_music),
                getString(R.string.category_sports),
                getString(R.string.category_arts_theatre),
                getString(R.string.category_film),
                getString(R.string.category_miscellaneous)
        };

        for (String category : categories) {
            categoryTabLayout.addTab(categoryTabLayout.newTab().setText(category));
        }

        categoryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedCategory = tab.getText().toString();
                // Apply client-side filtering when tab is selected
                filterEventsByCategory(selectedCategory);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        eventsAdapter = new EventsAdapter(this, this);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsRecyclerView.setAdapter(eventsAdapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> onBackPressed());
        searchButton.setOnClickListener(v -> performSearch());
        
        // Clear distance error when user types
        distanceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error when user starts typing
                if (distanceInputLayout.isErrorEnabled()) {
                    distanceInputLayout.setErrorEnabled(false);
                    distanceInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Add arrow key handling for distance input (left = decrease, right = increase)
        distanceEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    String currentText = distanceEditText.getText().toString().trim();
                    int currentValue;
                    
                    try {
                        if (currentText.isEmpty()) {
                            currentValue = 10; // Default value when empty
                        } else {
                            currentValue = Integer.parseInt(currentText);
                        }
                        
                        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                            // Decrease value
                            currentValue = Math.max(1, currentValue - 1);
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                            // Increase value
                            currentValue = Math.min(100, currentValue + 1);
                        }
                        
                        distanceEditText.setText(String.valueOf(currentValue));
                        distanceEditText.setSelection(distanceEditText.getText().length());
                        return true; // Consume the event
                    } catch (NumberFormatException e) {
                        // If parsing fails, set to default
                        distanceEditText.setText("10");
                        distanceEditText.setSelection(2);
                        return true;
                    }
                }
            }
            return false; // Let other keys be handled normally
        });
    }

    private void detectCurrentLocation() {
        // Call ipinfo.io directly
        DirectApiClient.getIpInfoService().getIpInfo()
                .enqueue(new Callback<IpInfoDirectResponse>() {
                    @Override
                    public void onResponse(Call<IpInfoDirectResponse> call, Response<IpInfoDirectResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            IpInfoDirectResponse ipInfo = response.body();
                            double lat = ipInfo.getLatitude();
                            double lng = ipInfo.getLongitude();
                            
                            // Generate geohash from lat/lng
                            if (lat != 0.0 && lng != 0.0) {
                                currentGeohash = GeohashUtil.encode(lat, lng);
                                Log.d(TAG, "Detected location from IP: " + ipInfo.getFormattedAddress() + 
                                        " -> (" + lat + ", " + lng + ") -> " + currentGeohash);
                            } else {
                                Log.e(TAG, "Invalid coordinates from ipinfo.io");
                                Toast.makeText(SearchActivity.this, 
                                        "Failed to detect location", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Failed to get IP info");
                            Toast.makeText(SearchActivity.this, 
                                    "Failed to detect location", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<IpInfoDirectResponse> call, Throwable t) {
                        Log.e(TAG, "IP info request failed: " + t.getMessage());
                        Toast.makeText(SearchActivity.this, 
                                "Failed to detect location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void detectCurrentLocationAndSearch(String keyword, String distance) {
        // Call ipinfo.io directly
        DirectApiClient.getIpInfoService().getIpInfo()
                .enqueue(new Callback<IpInfoDirectResponse>() {
                    @Override
                    public void onResponse(Call<IpInfoDirectResponse> call, Response<IpInfoDirectResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            IpInfoDirectResponse ipInfo = response.body();
                            double lat = ipInfo.getLatitude();
                            double lng = ipInfo.getLongitude();
                            
                            // Generate geohash from lat/lng
                            if (lat != 0.0 && lng != 0.0) {
                                String geohash = GeohashUtil.encode(lat, lng);
                                currentGeohash = geohash;
                                Log.d(TAG, "Detected location from IP: " + ipInfo.getFormattedAddress() + 
                                        " -> (" + lat + ", " + lng + ") -> " + geohash);
                                
                                // Now perform the search with the geohash (no segmentId)
                                executeSearch(keyword, distance, geohash);
                            } else {
                                showLoading(false);
                                Log.e(TAG, "Invalid coordinates from ipinfo.io");
                                Toast.makeText(SearchActivity.this, 
                                        "Failed to detect location", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            showLoading(false);
                            Log.e(TAG, "Failed to get IP info");
                            Toast.makeText(SearchActivity.this, 
                                    "Failed to detect location", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<IpInfoDirectResponse> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "IP info request failed: " + t.getMessage());
                        Toast.makeText(SearchActivity.this, 
                                "Failed to detect location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void geocodeAddressAndSearch(String address, String keyword, String distance) {
        // Call Google Geocoding API directly
        DirectApiClient.getGoogleGeocodingService().geocodeAddress(address, GOOGLE_API_KEY)
                .enqueue(new Callback<GoogleGeocodingResponse>() {
                    @Override
                    public void onResponse(Call<GoogleGeocodingResponse> call, Response<GoogleGeocodingResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            GoogleGeocodingResponse geocode = response.body();
                            if (geocode.isSuccessful()) {
                                double lat = geocode.getLatitude();
                                double lng = geocode.getLongitude();
                                
                                // Generate geohash from lat/lng
                                String geohash = GeohashUtil.encode(lat, lng);
                                currentGeohash = geohash;
                                
                                Log.d(TAG, "Geocoded address: " + address + " -> (" + lat + ", " + lng + ") -> " + geohash);
                                
                                // Now perform the search with the geohash (no segmentId)
                                executeSearch(keyword, distance, geohash);
                            } else {
                                showLoading(false);
                                Log.e(TAG, "Geocoding failed: " + geocode.getStatus());
                                Toast.makeText(SearchActivity.this, 
                                        "Failed to geocode location: " + geocode.getStatus(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            showLoading(false);
                            Log.e(TAG, "Geocoding failed");
                            Toast.makeText(SearchActivity.this, 
                                    "Failed to geocode location", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GoogleGeocodingResponse> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "Geocoding request failed: " + t.getMessage());
                        Toast.makeText(SearchActivity.this, 
                                "Failed to geocode location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void geocodeAddress(String address) {
        // Call Google Geocoding API directly
        DirectApiClient.getGoogleGeocodingService().geocodeAddress(address, GOOGLE_API_KEY)
                .enqueue(new Callback<GoogleGeocodingResponse>() {
                    @Override
                    public void onResponse(Call<GoogleGeocodingResponse> call, Response<GoogleGeocodingResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            GoogleGeocodingResponse geocode = response.body();
                            if (geocode.isSuccessful()) {
                                double lat = geocode.getLatitude();
                                double lng = geocode.getLongitude();
                                
                                // Generate geohash from lat/lng
                                currentGeohash = GeohashUtil.encode(lat, lng);
                                Log.d(TAG, "Geocoded address: " + address + " -> (" + lat + ", " + lng + ") -> " + currentGeohash);
                            } else {
                                Log.e(TAG, "Geocoding failed: " + geocode.getStatus());
                            }
                        } else {
                            Log.e(TAG, "Geocoding failed");
                        }
                    }

                    @Override
                    public void onFailure(Call<GoogleGeocodingResponse> call, Throwable t) {
                        Log.e(TAG, "Geocoding request failed: " + t.getMessage());
                    }
                });
    }

    private void performSearch() {
        String keyword = keywordEditText.getText().toString().trim();
        String distance = distanceEditText.getText().toString().trim();
        boolean hasErrors = false;

        // Validate keyword
        if (keyword.isEmpty()) {
            keywordInputLayout.setErrorEnabled(true);
            keywordInputLayout.setError("Please enter a keyword");
            hasErrors = true;
        } else {
            // Clear keyword error if valid
            if (keywordInputLayout.isErrorEnabled()) {
                keywordInputLayout.setErrorEnabled(false);
                keywordInputLayout.setError(null);
            }
        }

        // Validate location if custom location is selected
        if (isCustomLocation) {
            String customLocation = locationSpinner.getText().toString().trim();
            if (customLocation.isEmpty() || "Enter Location".equals(customLocation) || "Current Location".equals(customLocation)) {
                locationInputLayout.setErrorEnabled(true);
                locationInputLayout.setError("Please enter a location");
                hasErrors = true;
            } else {
                // Clear location error if valid
                if (locationInputLayout.isErrorEnabled()) {
                    locationInputLayout.setErrorEnabled(false);
                    locationInputLayout.setError(null);
                }
            }
        }

        // Validate distance
        if (distance.isEmpty()) {
            // Show error when field is empty
            distanceInputLayout.setErrorEnabled(true);
            distanceInputLayout.setError(getString(R.string.distance_validation_error));
            hasErrors = true;
        } else {
            try {
                int distanceValue = Integer.parseInt(distance);
                if (distanceValue < 1 || distanceValue > 100) {
                    distanceInputLayout.setErrorEnabled(true);
                    distanceInputLayout.setError(getString(R.string.distance_validation_error));
                    hasErrors = true;
                } else {
                    // Clear distance error if valid
                    if (distanceInputLayout.isErrorEnabled()) {
                        distanceInputLayout.setErrorEnabled(false);
                        distanceInputLayout.setError(null);
                    }
                }
            } catch (NumberFormatException e) {
                distanceInputLayout.setErrorEnabled(true);
                distanceInputLayout.setError(getString(R.string.distance_validation_error));
                hasErrors = true;
            }
        }

        // Return early if there are validation errors
        if (hasErrors) {
            return;
        }

        // Get geohash
        String geoPoint = currentGeohash;
        if (isCustomLocation) {
            String customLocation = locationSpinner.getText().toString().trim();
            
            // Need to geocode custom location first
            showLoading(true);
            geocodeCustomLocationAndSearch(customLocation, keyword, distance);
            return;
        }

        // If using current location but geohash is not ready yet, detect location and then search
        if (geoPoint.isEmpty()) {
            showLoading(true);
            detectCurrentLocationAndSearch(keyword, distance);
            return;
        }

        // Perform search (no segmentId - filtering done client-side)
        executeSearch(keyword, distance, geoPoint);
    }

    private void geocodeCustomLocationAndSearch(String location, String keyword, String distance) {
        // Call Google Geocoding API directly
        DirectApiClient.getGoogleGeocodingService().geocodeAddress(location, GOOGLE_API_KEY)
                .enqueue(new Callback<GoogleGeocodingResponse>() {
                    @Override
                    public void onResponse(Call<GoogleGeocodingResponse> call, Response<GoogleGeocodingResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            GoogleGeocodingResponse geocode = response.body();
                            if (geocode.isSuccessful()) {
                                double lat = geocode.getLatitude();
                                double lng = geocode.getLongitude();
                                
                                // Generate geohash from lat/lng
                                String geohash = GeohashUtil.encode(lat, lng);
                                
                                Log.d(TAG, "Geocoded custom location: " + location + " -> (" + lat + ", " + lng + ") -> " + geohash);
                                
                                // Clear any previous error since location is valid
                                if (locationInputLayout.isErrorEnabled()) {
                                    locationInputLayout.setErrorEnabled(false);
                                    locationInputLayout.setError(null);
                                }
                                
                                // Perform search (no segmentId - filtering done client-side)
                                executeSearch(keyword, distance, geohash);
                            } else {
                                showLoading(false);
                                Log.e(TAG, "Geocoding failed: " + geocode.getStatus());
                                locationInputLayout.setErrorEnabled(true);
                                locationInputLayout.setError("Invalid location");
                            }
                        } else {
                            showLoading(false);
                            locationInputLayout.setErrorEnabled(true);
                            locationInputLayout.setError("Invalid location");
                        }
                    }

                    @Override
                    public void onFailure(Call<GoogleGeocodingResponse> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "Geocoding failed: " + t.getMessage());
                        locationInputLayout.setErrorEnabled(true);
                        locationInputLayout.setError("Invalid location");
                    }
                });
    }

    private void executeSearch(String keyword, String distance, String geoPoint) {
        showLoading(true);

        Log.d(TAG, "Searching: keyword=" + keyword + ", distance=" + distance + 
                ", geoPoint=" + geoPoint + " (no segmentId - client-side filtering)");

        // Call search API without segmentId parameter
        ApiClient.getApiService().searchEvents(keyword, distance, "miles", geoPoint, null)
                .enqueue(new Callback<List<Event>>() {
                    @Override
                    public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                        showLoading(false);

                        if (response.isSuccessful()) {
                            List<Event> events = response.body();
                            if (events != null && !events.isEmpty()) {
                                // Store all events for client-side filtering
                                allEvents = new ArrayList<>(events);
                                
                                // Apply current category filter
                                filterEventsByCategory(selectedCategory);
                            } else {
                                allEvents = new ArrayList<>();
                                showNoResults();
                            }
                        } else {
                            Toast.makeText(SearchActivity.this,
                                    "Search failed: " + response.code(), Toast.LENGTH_SHORT).show();
                            showError();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Event>> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "Search failed: " + t.getMessage());
                        Toast.makeText(SearchActivity.this,
                                "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        showError();
                    }
                });
    }
    
    /**
     * Filter events by category on the client side
     * @param category Category name from tab selection
     */
    private void filterEventsByCategory(String category) {
        if (allEvents == null || allEvents.isEmpty()) {
            return;
        }
        
        if (eventsAdapter == null || resultsRecyclerView == null || noResultsTextView == null) {
            return;
        }
        
        List<Event> filteredEvents;
        
        if ("All".equals(category)) {
            // Show all events - no filtering
            filteredEvents = new ArrayList<>(allEvents);
        } else {
            // Filter by segment name from classifications
            filteredEvents = new ArrayList<>();
            for (Event event : allEvents) {
                if (event != null) {
                    String segmentName = event.getCategory(); // getCategory() returns segment name
                    if (category.equals(segmentName)) {
                        filteredEvents.add(event);
                    }
                }
            }
        }
        
        Log.d(TAG, "Filtered events: category=" + category + ", total=" + allEvents.size() + 
                ", filtered=" + filteredEvents.size());
        
        // Update adapter with filtered events
        if (!filteredEvents.isEmpty()) {
            eventsAdapter.setEvents(filteredEvents);
            resultsRecyclerView.setVisibility(View.VISIBLE);
            noResultsTextView.setVisibility(View.GONE);
        } else {
            showNoResults();
        }
    }

    private void showLoading(boolean show) {
        loadingContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        resultsRecyclerView.setVisibility(View.GONE);
        noResultsTextView.setVisibility(View.GONE);
    }

    private void showNoResults() {
        resultsRecyclerView.setVisibility(View.GONE);
        noResultsTextView.setVisibility(View.VISIBLE);
        loadingContainer.setVisibility(View.GONE);
    }

    private void showError() {
        resultsRecyclerView.setVisibility(View.GONE);
        noResultsTextView.setVisibility(View.VISIBLE);
        loadingContainer.setVisibility(View.GONE);
    }

    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(SearchActivity.this, EventDetailsActivity.class);
        intent.putExtra("event", event);
        eventDetailsLauncher.launch(intent);
    }
    
    private void updateEventInList(Event updatedEvent) {
        // Find and update the event in allEvents list
        if (updatedEvent == null || updatedEvent.getId() == null) {
            return;
        }
        for (int i = 0; i < allEvents.size(); i++) {
            Event event = allEvents.get(i);
            if (event != null && event.getId() != null && event.getId().equals(updatedEvent.getId())) {
                event.setFavorited(updatedEvent.isFavorited());
                Log.d(TAG, "Updated event favorite status: " + event.getName() + " -> " + event.isFavorited());
                break;
            }
        }
    }

    @Override
    public void onFavoriteClick(Event event) {
        if (event == null || event.getId() == null || event.getId().isEmpty()) {
            Log.e(TAG, "Cannot toggle favorite: event or event ID is null/empty");
            return;
        }
        
        if (eventsAdapter == null) {
            Log.e(TAG, "Cannot toggle favorite: eventsAdapter is null");
            return;
        }
        
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
                                String message = (event.getName() != null ? event.getName() : "Event") + " added to favorites!";
                                Toast.makeText(SearchActivity.this, message, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Event added to favorites: " + event.getName());
                            } else {
                                // Revert the state if API call failed
                                event.setFavorited(false);
                                if (eventsAdapter != null) {
                                    eventsAdapter.notifyDataSetChanged();
                                }
                                Toast.makeText(SearchActivity.this, 
                                        "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Failed to add favorite. Response code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            // Revert the state if API call failed
                            event.setFavorited(false);
                            if (eventsAdapter != null) {
                                eventsAdapter.notifyDataSetChanged();
                            }
                            Log.e(TAG, "Error adding to favorites", t);
                            String errorMsg = t != null && t.getMessage() != null ? t.getMessage() : "Unknown error";
                            Toast.makeText(SearchActivity.this, 
                                    "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Remove from favorites - DELETE request
            ApiClient.getApiService().removeFavorite(event.getId())
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                String message = (event.getName() != null ? event.getName() : "Event") + " removed from favorites";
                                Toast.makeText(SearchActivity.this, message, Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Event removed from favorites: " + event.getName());
                            } else {
                                // Revert the state if API call failed
                                event.setFavorited(true);
                                if (eventsAdapter != null) {
                                    eventsAdapter.notifyDataSetChanged();
                                }
                                Toast.makeText(SearchActivity.this, 
                                        "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Failed to remove favorite. Response code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            // Revert the state if API call failed
                            event.setFavorited(true);
                            if (eventsAdapter != null) {
                                eventsAdapter.notifyDataSetChanged();
                            }
                            Log.e(TAG, "Error removing from favorites", t);
                            String errorMsg = t != null && t.getMessage() != null ? t.getMessage() : "Unknown error";
                            Toast.makeText(SearchActivity.this, 
                                    "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Note: Removed notifyDataSetChanged() call for performance
        // The adapter is already updated via ActivityResultLauncher callback
        // Calling notifyDataSetChanged() here causes unnecessary full redraws
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autocompleteRunnable != null) {
            autocompleteHandler.removeCallbacks(autocompleteRunnable);
        }
        if (locationSearchRunnable != null) {
            locationSearchHandler.removeCallbacks(locationSearchRunnable);
        }
    }
}
