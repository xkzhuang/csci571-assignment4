package com.example.myapplication.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapters.AlbumsAdapter;
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.models.Event;
import com.example.myapplication.models.EventDetails;
import com.example.myapplication.models.SpotifyAlbum;
import com.example.myapplication.models.SpotifyArtist;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistTabFragment extends Fragment {
    
    private static final String TAG = "ArtistTabFragment";
    
    private MaterialCardView artistInfoCard;
    private ImageView artistImageView;
    private TextView artistNameTextView;
    private TextView followersTextView;
    private TextView popularityTextView;
    private ImageButton externalLinkButton;
    private ChipGroup genresChipGroup;
    private TextView albumsTitleTextView;
    private MaterialCardView noArtistCard;
    private TextView noArtistMessageTextView;
    private RecyclerView albumsRecyclerView;
    private com.google.android.material.progressindicator.CircularProgressIndicator loadingProgressBar;
    
    private Event event;
    private EventDetails eventDetails;
    private AlbumsAdapter albumsAdapter;
    
    // Track loading state for both API calls
    private boolean artistInfoLoaded = false;
    private boolean albumsLoaded = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_tab, container, false);
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
        artistInfoCard = view.findViewById(R.id.artistInfoCard);
        artistImageView = view.findViewById(R.id.artistImageView);
        artistNameTextView = view.findViewById(R.id.artistNameTextView);
        followersTextView = view.findViewById(R.id.followersTextView);
        popularityTextView = view.findViewById(R.id.popularityTextView);
        externalLinkButton = view.findViewById(R.id.externalLinkButton);
        genresChipGroup = view.findViewById(R.id.genresChipGroup);
        albumsTitleTextView = view.findViewById(R.id.albumsTitleTextView);
        noArtistCard = view.findViewById(R.id.noArtistCard);
        noArtistMessageTextView = view.findViewById(R.id.noArtistMessageTextView);
        albumsRecyclerView = view.findViewById(R.id.albumsRecyclerView);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        
        // Setup RecyclerView with GridLayoutManager (2 columns)
        setupRecyclerView();
        
        // Only fetch Spotify info for Music segment
        if (isMusicSegment()) {
            fetchArtistInfo();
        } else {
            Log.d(TAG, "Event segment is not Music. Skipping Spotify lookup.");
            // Hide loading indicator since we're not making any API calls
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.GONE);
            }
            showNoArtistMessage(getString(R.string.no_artist_data));
        }
    }
    
    private void setupRecyclerView() {
        albumsAdapter = new AlbumsAdapter(requireContext());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        albumsRecyclerView.setLayoutManager(gridLayoutManager);
        albumsRecyclerView.setAdapter(albumsAdapter);
    }
    
    private void fetchArtistInfo() {
        // Reset loading state
        artistInfoLoaded = false;
        albumsLoaded = false;
        
        // Show loading indicator
        showLoading(true);
        
        // Get artist name from eventDetails or event
        final String artistName = getArtistName();
        
        if (artistName == null || artistName.isEmpty()) {
            Log.w(TAG, "No artist name available");
            artistInfoLoaded = true;
            albumsLoaded = true; // Mark as loaded since we won't fetch albums
            showLoading(false);
            showNoArtistMessage(getString(R.string.no_artist_data));
            return;
        }
        
        Log.d(TAG, "Fetching Spotify artist info for: " + artistName);
        
        // Fetch artist info
        ApiClient.getApiService().getSpotifyArtist(artistName)
                .enqueue(new Callback<SpotifyArtist>() {
                    @Override
                    public void onResponse(Call<SpotifyArtist> call, Response<SpotifyArtist> response) {
                        artistInfoLoaded = true;
                        
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                SpotifyArtist artist = response.body();
                                Log.d(TAG, "Artist info loaded successfully: " + artist.getName());
                                displayArtistInfo(artist);
                                
                                // Fetch albums separately
                                fetchAlbums(artistName);
                            } else {
                                Log.w(TAG, "Spotify artist response body is null");
                                albumsLoaded = true; // Mark as loaded since we won't fetch albums
                                checkAndHideLoading();
                                showNoArtistMessage(getString(R.string.no_artist_data));
                            }
                        } else {
                            Log.e(TAG, "Failed to load artist info: " + response.code());
                            albumsLoaded = true; // Mark as loaded since we won't fetch albums
                            checkAndHideLoading();
                            if (response.code() >= 400 && response.code() < 600) {
                                showNoArtistMessage(getString(R.string.spotify_error_message));
                            } else {
                                showNoArtistMessage(getString(R.string.no_artist_data));
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<SpotifyArtist> call, Throwable t) {
                        Log.e(TAG, "Error loading artist info", t);
                        artistInfoLoaded = true;
                        albumsLoaded = true; // Mark as loaded since we won't fetch albums
                        checkAndHideLoading();
                        showNoArtistMessage(getString(R.string.no_artist_data));
                    }
                });
    }
    
    private void fetchAlbums(String artistName) {
        Log.d(TAG, "Fetching Spotify albums for: " + artistName);
        
        ApiClient.getApiService().getSpotifyAlbums(artistName)
                .enqueue(new Callback<List<SpotifyAlbum>>() {
                    @Override
                    public void onResponse(Call<List<SpotifyAlbum>> call, Response<List<SpotifyAlbum>> response) {
                        albumsLoaded = true;
                        
                        if (response.isSuccessful() && response.body() != null) {
                            List<SpotifyAlbum> albums = response.body();
                            Log.d(TAG, "Albums loaded successfully: " + albums.size() + " albums");
                            displayAlbums(albums);
                        } else {
                            Log.e(TAG, "Failed to load albums: " + response.code());
                        }
                        
                        checkAndHideLoading();
                    }
                    
                    @Override
                    public void onFailure(Call<List<SpotifyAlbum>> call, Throwable t) {
                        Log.e(TAG, "Error loading albums", t);
                        albumsLoaded = true;
                        checkAndHideLoading();
                    }
                });
    }
    
    private String getArtistName() {
        // Try to get from eventDetails first
        if (eventDetails != null) {
            List<EventDetails.Attraction> attractions = eventDetails.getAttractions();
            if (attractions != null && !attractions.isEmpty()) {
                EventDetails.Attraction attraction = attractions.get(0);
                if (attraction != null && attraction.getName() != null) {
                    return attraction.getName();
                }
            }
        }
        
        // Fallback: Try to extract from event name or category
        // This is a fallback in case eventDetails is not available
        return null;
    }
    
    private void displayArtistInfo(SpotifyArtist artist) {
        hideNoArtistMessage();
        
        // Artist image
        if (artist.getImageUrl() != null && !artist.getImageUrl().isEmpty()) {
            Glide.with(requireContext())
                    .load(artist.getImageUrl())
                    .centerCrop()
                    .into(artistImageView);
        }
        
        // Artist name
        artistNameTextView.setText(artist.getName());
        
        // Followers with number formatting
        int followersCount = artist.getFollowersCount();
        String followersFormatted = NumberFormat.getNumberInstance(Locale.US).format(followersCount);
        followersTextView.setText(followersFormatted);
        
        // Popularity
        int popularity = artist.getPopularity();
        popularityTextView.setText(popularity + "%");
        
        // Genres
        displayGenres(artist.getGenres());
        
        // External link button (Open in Spotify)
        final String spotifyUrl = artist.getSpotifyUrl();
        externalLinkButton.setOnClickListener(v -> {
            if (spotifyUrl != null && !spotifyUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUrl));
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Spotify URL not available", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayGenres(List<String> genres) {
        genresChipGroup.removeAllViews();
        
        if (genres != null && !genres.isEmpty()) {
            for (String genre : genres) {
                Chip chip = new Chip(requireContext());
                chip.setText(genre);
                chip.setClickable(false);
                genresChipGroup.addView(chip);
            }
        }
    }
    
    private void displayAlbums(List<SpotifyAlbum> albums) {
        if (albums != null && !albums.isEmpty()) {
            albumsAdapter.setAlbums(albums);
            albumsRecyclerView.setVisibility(View.VISIBLE);
        } else {
            albumsRecyclerView.setVisibility(View.GONE);
        }
    }
    
    private void showNoArtistMessage(String message) {
        if (noArtistMessageTextView != null) {
            noArtistMessageTextView.setText(message);
        }
        if (noArtistCard != null) {
            noArtistCard.setVisibility(View.VISIBLE);
        }
        if (artistInfoCard != null) {
            artistInfoCard.setVisibility(View.GONE);
        }
        if (albumsTitleTextView != null) {
            albumsTitleTextView.setVisibility(View.GONE);
        }
        if (albumsRecyclerView != null) {
            albumsRecyclerView.setVisibility(View.GONE);
        }
    }
    
    private void hideNoArtistMessage() {
        if (noArtistCard != null) {
            noArtistCard.setVisibility(View.GONE);
        }
        if (artistInfoCard != null) {
            artistInfoCard.setVisibility(View.VISIBLE);
        }
        if (albumsTitleTextView != null) {
            albumsTitleTextView.setVisibility(View.VISIBLE);
        }
    }
    
    private boolean isMusicSegment() {
        if (event == null || event.getClassifications() == null || event.getClassifications().isEmpty()) {
            return false;
        }
        Event.Classification classification = event.getClassifications().get(0);
        if (classification != null && classification.getSegment() != null && classification.getSegment().getName() != null) {
            return "music".equalsIgnoreCase(classification.getSegment().getName());
        }
        return false;
    }
    
    private void showLoading(boolean show) {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        // Hide content while loading
        if (show) {
            if (artistInfoCard != null) {
                artistInfoCard.setVisibility(View.GONE);
            }
            if (albumsTitleTextView != null) {
                albumsTitleTextView.setVisibility(View.GONE);
            }
            if (albumsRecyclerView != null) {
                albumsRecyclerView.setVisibility(View.GONE);
            }
            if (noArtistCard != null) {
                noArtistCard.setVisibility(View.GONE);
            }
        }
    }
    
    private void checkAndHideLoading() {
        // Only hide loading when both API calls are finished
        if (artistInfoLoaded && albumsLoaded) {
            showLoading(false);
        }
    }
}
