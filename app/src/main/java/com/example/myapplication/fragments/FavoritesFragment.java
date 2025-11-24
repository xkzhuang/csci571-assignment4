package com.example.myapplication.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.EventDetailsActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapters.FavoritesAdapter;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.api.ApiService;
import com.example.myapplication.models.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesFragment extends Fragment implements FavoritesAdapter.OnEventClickListener {
    
    private static final String TAG = "FavoritesFragment";
    
    private TextView currentDateTextView;
    private RecyclerView favoritesRecyclerView;
    private View noFavoritesContainer;
    private TextView noFavoritesTextView;
    private TextView poweredByTicketMaster;
    private ProgressBar loadingProgressBar;
    
    private FavoritesAdapter favoritesAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        currentDateTextView = view.findViewById(R.id.currentDateTextView);
        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView);
        noFavoritesContainer = view.findViewById(R.id.noFavoritesContainer);
        noFavoritesTextView = view.findViewById(R.id.noFavoritesTextView);
        poweredByTicketMaster = view.findViewById(R.id.poweredByTicketMaster);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        
        // Setup "Powered by TicketMaster" click handler
        poweredByTicketMaster.setOnClickListener(v -> {
            String url = getString(R.string.ticketmaster_url);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });
        
        // Set current date
        setCurrentDate();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Load favorites from backend
        loadFavoritesFromBackend();
    }
    
    private void setCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
        String currentDate = dateFormat.format(new Date());
        currentDateTextView.setText(currentDate);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload favorites when fragment becomes visible
        loadFavoritesFromBackend();
        // Start updating timestamps every second
        if (favoritesAdapter != null) {
            favoritesAdapter.startTimestampUpdates();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Stop updating timestamps to save resources
        if (favoritesAdapter != null) {
            favoritesAdapter.stopTimestampUpdates();
        }
    }
    
    private void setupRecyclerView() {
        favoritesAdapter = new FavoritesAdapter(requireContext(), this);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        favoritesRecyclerView.setAdapter(favoritesAdapter);
    }
    
    private void loadFavoritesFromBackend() {
        // Show loading
        showLoading(true);
        
        Log.d(TAG, "Loading favorites from backend...");
        Log.d(TAG, "URL: https://xkzhuang-csci571-hw03-820224420769.us-west1.run.app/api/manage/favorites");
        
        // Fetch favorites from backend API
        ApiClient.getApiService().getFavorites()
                .enqueue(new Callback<List<Event>>() {
                    @Override
                    public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                        showLoading(false);
                        
                        // Log response details
                        Log.d(TAG, "Response Code: " + response.code());
                        Log.d(TAG, "Response Message: " + response.message());
                        Log.d(TAG, "Request URL: " + call.request().url());
                        Log.d(TAG, "Response Headers: " + response.headers());
                        
                        if (response.isSuccessful()) {
                            List<Event> favorites = response.body();
                            
                            if (favorites != null) {
                                Log.d(TAG, "Favorites count: " + favorites.size());
                                if (!favorites.isEmpty()) {
                                    showFavorites(favorites);
                                } else {
                                    Log.d(TAG, "Favorites list is empty");
                                    showNoFavorites();
                                }
                            } else {
                                Log.w(TAG, "Favorites list is null");
                                showNoFavorites();
                            }
                        } else {
                            // Log error response
                            Log.e(TAG, "Response not successful. Code: " + response.code());
                            String errorBody = "Unable to read error body";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                    Log.e(TAG, "Error Body: " + errorBody);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body", e);
                            }
                            
                            Toast.makeText(requireContext(), 
                                    "Error: " + response.code() + " - " + response.message(), 
                                    Toast.LENGTH_LONG).show();
                            showNoFavorites();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<List<Event>> call, Throwable t) {
                        showLoading(false);
                        
                        // Log full stack trace
                        Log.e(TAG, "=== Network request FAILED ===");
                        Log.e(TAG, "Error type: " + t.getClass().getName());
                        Log.e(TAG, "Error message: " + t.getMessage());
                        Log.e(TAG, "Request URL: " + call.request().url());
                        Log.e(TAG, "Full stack trace:", t);
                        
                        // Check if it's a JSON parsing error
                        String errorMessage;
                        if (t instanceof com.google.gson.JsonSyntaxException || 
                            t.getMessage() != null && t.getMessage().contains("Expected")) {
                            errorMessage = "JSON parsing error - backend response format doesn't match expected structure";
                            Log.e(TAG, "This is a JSON parsing error. The favorites API might return a different format.");
                        } else {
                            errorMessage = "Network error: " + t.getMessage();
                        }
                        
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                        showNoFavorites();
                    }
                });
    }
    
    private void showLoading(boolean show) {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        favoritesRecyclerView.setVisibility(View.GONE);
        if (noFavoritesContainer != null) {
            noFavoritesContainer.setVisibility(View.GONE);
        }
    }
    
    private void showFavorites(List<Event> favorites) {
        favoritesAdapter.setEvents(favorites);
        favoritesRecyclerView.setVisibility(View.VISIBLE);
        if (noFavoritesContainer != null) {
            noFavoritesContainer.setVisibility(View.GONE);
        }
        // Start updating timestamps
        favoritesAdapter.startTimestampUpdates();
    }
    
    private void showNoFavorites() {
        favoritesRecyclerView.setVisibility(View.GONE);
        if (noFavoritesContainer != null) {
            noFavoritesContainer.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop timestamp updates to prevent memory leaks
        if (favoritesAdapter != null) {
            favoritesAdapter.stopTimestampUpdates();
        }
    }
    
    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(requireContext(), EventDetailsActivity.class);
        intent.putExtra("event", event);
        startActivity(intent);
    }
}

