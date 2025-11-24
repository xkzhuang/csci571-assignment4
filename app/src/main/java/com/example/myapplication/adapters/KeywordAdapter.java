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
 * Custom adapter for keyword dropdown with support for:
 * - User input as first option
 * - "Searching..." item with loading indicator
 * - Autocomplete suggestions
 */
public class KeywordAdapter extends ArrayAdapter<String> {
    
    private final Context context;
    private final List<String> items;
    private String userInput = "";
    private boolean showSearching = false;
    
    public KeywordAdapter(@NonNull Context context, @NonNull List<String> items) {
        super(context, R.layout.item_location_dropdown, items);
        this.context = context;
        this.items = items;
    }
    
    @Override
    public int getCount() {
        int count = items.size();
        if (!userInput.isEmpty()) count++; // User input as first option
        if (showSearching) count++; // "Searching..." option
        return count;
    }
    
    @Override
    public String getItem(int position) {
        if (!userInput.isEmpty() && position == 0) {
            return userInput;
        }
        int offset = userInput.isEmpty() ? 0 : 1;
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
        
        // Hide loading indicator and icon by default
        loadingIndicator.setVisibility(View.GONE);
        icon.setVisibility(View.GONE);
        
        String item = getItem(position);
        
        if (item == null) {
            text.setText("");
            return convertView;
        }
        
        if ("Searching...".equals(item)) {
            // Show loading indicator for Searching state
            icon.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.VISIBLE);
            text.setText("Searching...");
        } else {
            // Hide icon for keyword items (user input or suggestions)
            icon.setVisibility(View.GONE);
            text.setText(item);
        }
        
        return convertView;
    }
    
    public void setUserInput(String input) {
        String newInput = input != null ? input : "";
        // Only update if input actually changed to avoid unnecessary notifyDataSetChanged() calls
        if (userInput == null ? newInput != null : !userInput.equals(newInput)) {
            userInput = newInput;
            // Post the update to avoid blocking input connection
            notifyDataSetChanged();
        }
    }
    
    public void setShowSearching(boolean show) {
        if (showSearching != show) {
            showSearching = show;
            notifyDataSetChanged();
        }
    }
    
    public void updateSuggestions(List<String> suggestions) {
        boolean changed = false;
        if (suggestions == null) {
            if (!items.isEmpty()) {
                items.clear();
                changed = true;
            }
        } else {
            // Only update if suggestions actually changed
            if (items.size() != suggestions.size() || !items.equals(suggestions)) {
                items.clear();
                items.addAll(suggestions);
                changed = true;
            }
        }
        
        if (changed) {
            notifyDataSetChanged();
        }
    }
}

