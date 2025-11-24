package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

/**
 * Custom adapter for location dropdown with support for:
 * - "Current Location" item with icon at the top
 * - "Searching..." item with loading indicator
 * - Google Places search results
 */
public class LocationAdapter extends ArrayAdapter<String> {
    
    private final Context context;
    private final List<String> items;
    private boolean showCurrentLocation = true;
    private boolean showSearching = false;
    
    public LocationAdapter(@NonNull Context context, @NonNull List<String> items) {
        super(context, R.layout.item_location_dropdown, items);
        this.context = context;
        this.items = items;
    }
    
    @Override
    public int getCount() {
        int count = items.size();
        if (showCurrentLocation) count++;
        if (showSearching) count++;
        return count;
    }
    
    @Override
    public String getItem(int position) {
        if (showCurrentLocation && position == 0) {
            return "Current Location";
        }
        int offset = showCurrentLocation ? 1 : 0;
        if (showSearching && position == offset) {
            return "Searching...";
        }
        int searchOffset = showSearching ? 1 : 0;
        int itemIndex = position - offset - searchOffset;
        if (itemIndex >= 0 && itemIndex < items.size()) {
            return items.get(itemIndex);
        }
        return null;
    }
    
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_location_dropdown, parent, false);
        }
        
        ImageView icon = convertView.findViewById(R.id.locationIcon);
        TextView text = convertView.findViewById(R.id.locationText);
        CircularProgressIndicator loadingIndicator = convertView.findViewById(R.id.loadingIndicator);
        
        String item = getItem(position);
        
        // Hide loading indicator by default
        loadingIndicator.setVisibility(View.GONE);
        icon.setVisibility(View.VISIBLE);
        
        if (item == null) {
            text.setText("");
            return convertView;
        }
        
        if ("Current Location".equals(item)) {
            // Show location pin icon for Current Location
            icon.setImageResource(R.drawable.ic_location);
            icon.setVisibility(View.VISIBLE);
            text.setText("Current Location");
        } else if ("Searching...".equals(item)) {
            // Show loading indicator for Searching state
            icon.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.VISIBLE);
            text.setText("Searching...");
        } else {
            // Show location icon for search results
            icon.setImageResource(R.drawable.ic_location);
            icon.setVisibility(View.VISIBLE);
            text.setText(item);
        }
        
        return convertView;
    }
    
    public void setShowCurrentLocation(boolean show) {
        if (showCurrentLocation != show) {
            showCurrentLocation = show;
            notifyDataSetChanged();
        }
    }
    
    public void setShowSearching(boolean show) {
        if (showSearching != show) {
            showSearching = show;
            notifyDataSetChanged();
        }
    }
    
    public void updateSearchResults(List<String> results) {
        items.clear();
        if (results != null) {
            items.addAll(results);
        }
        notifyDataSetChanged();
    }
}

