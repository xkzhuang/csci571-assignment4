package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.adapters.ViewPagerAdapter;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.fragments.ArtistTabFragment;
import com.example.myapplication.fragments.InfoTabFragment;
import com.example.myapplication.fragments.VenueTabFragment;
import com.example.myapplication.models.Event;
import com.example.myapplication.models.EventDetails;
import com.example.myapplication.utils.FavoritesManager;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsActivity extends AppCompatActivity {
    
    private static final String TAG = "EventDetailsActivity";
    
    private Toolbar toolbar;
    private TextView eventTitleTextView;
    private ImageView favoriteIcon;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProgressBar loadingProgressBar;
    
    private Event event;
    private EventDetails eventDetails;
    private FavoritesManager favoritesManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable Dynamic Colors (Material You) - adapts to wallpaper on Android 12+
        DynamicColors.applyToActivityIfAvailable(this);
        
        setContentView(R.layout.activity_event_info);
        
        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        eventTitleTextView = findViewById(R.id.eventTitleTextView);
        favoriteIcon = findViewById(R.id.favoriteIcon);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        
        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Disable default title
        }
        
        // Initialize favorites manager
        favoritesManager = FavoritesManager.getInstance(this);
        
        // Get event from intent
        event = (Event) getIntent().getSerializableExtra("event");
        if (event == null) {
            finish();
            return;
        }
        
        // Setup UI
        setupEventDetails();
        setupListeners();
        
        // Fetch detailed event info from backend
        fetchEventDetails();
    }
    
    private void setupEventDetails() {
        // Set event name in marquee text view
        eventTitleTextView.setText(event.getName());
        updateFavoriteIcon();
        
        // If eventDetails loaded, update with its isFavorited status
        if (eventDetails != null) {
            event.setFavorited(eventDetails.isFavorited());
            updateFavoriteIcon();
        }
    }
    
    private void fetchEventDetails() {
        if (event == null || event.getId() == null || event.getId().isEmpty()) {
            Log.e(TAG, "Cannot fetch event details: event or event ID is null/empty");
            Toast.makeText(EventDetailsActivity.this, 
                    "Invalid event data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        showLoading(true);
        
        Log.d(TAG, "Fetching event details for ID: " + event.getId());
        
        ApiClient.getApiService().getEventDetails(event.getId())
                .enqueue(new Callback<EventDetails>() {
                    @Override
                    public void onResponse(Call<EventDetails> call, Response<EventDetails> response) {
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            eventDetails = response.body();
                            Log.d(TAG, "Event details loaded successfully");
                            
                            // Update event's favorited status from eventDetails
                            event.setFavorited(eventDetails.isFavorited());
                            updateFavoriteIcon();
                            
                            setupViewPager();
                        } else {
                            Log.e(TAG, "Failed to load event details: " + response.code());
                            Toast.makeText(EventDetailsActivity.this, 
                                    "Failed to load event details", Toast.LENGTH_SHORT).show();
                            // Still setup view pager with basic event data
                            setupViewPager();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<EventDetails> call, Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "Error loading event details", t);
                        Toast.makeText(EventDetailsActivity.this, 
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        // Still setup view pager with basic event data
                        setupViewPager();
                    }
                });
    }
    
    private void setupViewPager() {
        if (viewPager == null || tabLayout == null) {
            Log.e(TAG, "Cannot setup view pager: viewPager or tabLayout is null");
            return;
        }
        
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        
        // Create tab fragments with event data
        Bundle bundle = new Bundle();
        if (eventDetails != null) {
            bundle.putSerializable("eventDetails", eventDetails);
        }
        if (event != null) {
            bundle.putSerializable("event", event);
        }
        
        InfoTabFragment infoFragment = new InfoTabFragment();
        infoFragment.setArguments(bundle);
        
        ArtistTabFragment artistFragment = new ArtistTabFragment();
        artistFragment.setArguments(bundle);
        
        VenueTabFragment venueFragment = new VenueTabFragment();
        venueFragment.setArguments(bundle);
        
        adapter.addFragment(infoFragment, getString(R.string.tab_details));
        adapter.addFragment(artistFragment, getString(R.string.tab_artist));
        adapter.addFragment(venueFragment, getString(R.string.tab_venue));
        
        viewPager.setAdapter(adapter);
        
        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText(adapter.getPageTitle(position));
                    // Set icon for each tab
                    switch (position) {
                        case 0:
                            tab.setIcon(R.drawable.ic_details);
                            break;
                        case 1:
                            tab.setIcon(R.drawable.ic_artist);
                            break;
                        case 2:
                            tab.setIcon(R.drawable.ic_venue);
                            break;
                    }
                }
        ).attach();
    }
    
    private void showLoading(boolean show) {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (viewPager != null) {
            viewPager.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    
    private void setupListeners() {
        if (favoriteIcon == null || event == null || event.getId() == null || event.getId().isEmpty()) {
            Log.e(TAG, "Cannot setup listeners: favoriteIcon, event, or event ID is null/empty");
            return;
        }
        
        // Favorite icon click - Call backend API to add/remove
        favoriteIcon.setOnClickListener(v -> {
            // Toggle the state immediately for responsive UI
            boolean wasFavorited = event.isFavorited();
            event.setFavorited(!wasFavorited);
            updateFavoriteIcon();
            
            // Make the appropriate API call based on the new state
            if (event.isFavorited()) {
                // Add to favorites - POST request
                ApiService.FavoriteRequest request = new ApiService.FavoriteRequest(event, event.getId());
                ApiClient.getApiService().addFavorite(request)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    String message = (event.getName() != null ? event.getName() : "Event") + " added to favorites!";
                                    Toast.makeText(EventDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Event added to favorites: " + event.getName());
                                } else {
                                    // Revert the state if API call failed
                                    event.setFavorited(false);
                                    updateFavoriteIcon();
                                    Toast.makeText(EventDetailsActivity.this, 
                                            "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Failed to add favorite. Response code: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                // Revert the state if API call failed
                                event.setFavorited(false);
                                updateFavoriteIcon();
                                Log.e(TAG, "Error adding to favorites", t);
                                String errorMsg = t != null && t.getMessage() != null ? t.getMessage() : "Unknown error";
                                Toast.makeText(EventDetailsActivity.this, 
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
                                    Toast.makeText(EventDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Event removed from favorites: " + event.getName());
                                } else {
                                    // Revert the state if API call failed
                                    event.setFavorited(true);
                                    updateFavoriteIcon();
                                    Toast.makeText(EventDetailsActivity.this, 
                                            "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Failed to remove favorite. Response code: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                // Revert the state if API call failed
                                event.setFavorited(true);
                                updateFavoriteIcon();
                                Log.e(TAG, "Error removing from favorites", t);
                                String errorMsg = t != null && t.getMessage() != null ? t.getMessage() : "Unknown error";
                                Toast.makeText(EventDetailsActivity.this, 
                                        "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
    
    private void updateFavoriteIcon() {
        // Use event's isFavorited status from backend
        boolean isFavorite = event.isFavorited();
        favoriteIcon.setImageResource(isFavorite ?
                R.drawable.ic_star_filled : R.drawable.ic_star_outline);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finishWithResult();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        finishWithResult();
    }
    
    private void finishWithResult() {
        // Pass back the updated event with its favorite state
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updated_event", event);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }
}

