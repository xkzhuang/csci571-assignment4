package com.example.myapplication.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Event;
import com.example.myapplication.utils.DateFormatter;

import java.util.ArrayList;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {
    
    private List<Event> events;
    private Context context;
    private OnEventClickListener listener;
    
    public interface OnEventClickListener {
        void onEventClick(Event event);
        void onFavoriteClick(Event event);
    }
    
    public EventsAdapter(Context context, OnEventClickListener listener) {
        this.context = context;
        this.events = new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }
    
    @Override
    public int getItemCount() {
        return events.size();
    }
    
    public void setEvents(List<Event> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void addEvents(List<Event> newEvents) {
        if (newEvents != null) {
            this.events.addAll(newEvents);
            notifyDataSetChanged();
        }
    }
    
    public void clearEvents() {
        this.events.clear();
        notifyDataSetChanged();
    }
    
    class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImageView;
        TextView categoryBadge;
        TextView dateTimeBadge;
        TextView eventNameTextView;
        TextView venueTextView;
        ImageView favoriteIcon;
        
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImageView = itemView.findViewById(R.id.eventImageView);
            categoryBadge = itemView.findViewById(R.id.categoryBadge);
            dateTimeBadge = itemView.findViewById(R.id.dateTimeBadge);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            venueTextView = itemView.findViewById(R.id.venueTextView);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < events.size() && listener != null) {
                    Event event = events.get(position);
                    if (event != null) {
                        listener.onEventClick(event);
                    }
                }
            });
            
            favoriteIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < events.size() && listener != null) {
                    Event event = events.get(position);
                    if (event != null) {
                        // Toggle favorite status immediately for responsive UI
                        event.setFavorited(!event.isFavorited());
                        updateFavoriteIcon(event);
                        // Notify listener to make API call
                        listener.onFavoriteClick(event);
                    }
                }
            });
        }
        
        public void bind(Event event) {
            if (event == null) {
                return;
            }
            
            // Load event image
            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(event.getImageUrl())
                        .centerCrop()
                        .into(eventImageView);
            } else {
                eventImageView.setImageResource(R.drawable.ic_location);
            }
            
            // Set category badge (genre)
            String category = event.getCategory();
            categoryBadge.setText(category != null ? category : "");
            // Background color is set in XML as light gray
            
            // Set date/time badge with formatted date
            String formattedDate = DateFormatter.formatEventDate(event.getDateTime());
            dateTimeBadge.setText(formattedDate != null ? formattedDate : "");
            
            // Set event name
            String eventName = event.getName();
            eventNameTextView.setText(eventName != null ? eventName : "");
            
            // Set venue name
            String venueName = event.getVenue();
            venueTextView.setText(venueName != null && !venueName.isEmpty() ? venueName : "");
            
            // Update favorite icon based on isFavorited from backend
            updateFavoriteIcon(event);
        }
        
        private void updateFavoriteIcon(Event event) {
            // Use backend's isFavorited status
            boolean isFavorite = event.isFavorited();
            favoriteIcon.setImageResource(isFavorite ? 
                    R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        }
        
    }
}

