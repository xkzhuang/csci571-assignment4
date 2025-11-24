package com.example.myapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class FavoritesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_FOOTER = 1;
    
    // Static formatters to avoid creating new instances on every call (performance optimization)
    // Note: SimpleDateFormat is not thread-safe, but since all UI operations happen on main thread, this is safe
    private static final SimpleDateFormat INPUT_FORMAT_WITH_TIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
    private static final SimpleDateFormat INPUT_FORMAT_DATE_ONLY = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private static final SimpleDateFormat OUTPUT_FORMAT_EVENT_DATE = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.ENGLISH);
    private static final SimpleDateFormat UTC_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH);
    
    static {
        // Initialize timezones once
        INPUT_FORMAT_WITH_TIME.setTimeZone(TimeZone.getTimeZone("UTC"));
        UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    private List<Event> events;
    private Context context;
    private OnEventClickListener listener;
    private Handler updateHandler;
    private Runnable updateRunnable;
    private Set<FavoriteViewHolder> activeViewHolders;
    
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }
    
    public FavoritesAdapter(Context context, OnEventClickListener listener) {
        this.context = context;
        this.events = new ArrayList<>();
        this.listener = listener;
        this.activeViewHolders = new HashSet<>();
        this.updateHandler = new Handler(Looper.getMainLooper());
        
        // Create a runnable that updates all active view holders every second
        this.updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateAllTimestamps();
                updateHandler.postDelayed(this, 1000); // Update every second
            }
        };
    }
    
    @Override
    public int getItemViewType(int position) {
        if (position < events.size()) {
            return VIEW_TYPE_ITEM;
        } else {
            return VIEW_TYPE_FOOTER;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_favorite_footer, parent, false);
            return new FooterViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_favorite_card, parent, false);
            return new FavoriteViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FavoriteViewHolder) {
            Event event = events.get(position);
            ((FavoriteViewHolder) holder).bind(event);
        } else if (holder instanceof FooterViewHolder) {
            ((FooterViewHolder) holder).bind();
        }
    }
    
    @Override
    public int getItemCount() {
        // Add 1 for the footer
        return events.size() + 1;
    }
    
    public void setEvents(List<Event> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    /**
     * Start the timer to update timestamps every second
     */
    public void startTimestampUpdates() {
        updateHandler.removeCallbacks(updateRunnable);
        updateHandler.post(updateRunnable);
    }
    
    /**
     * Stop the timer to prevent memory leaks
     */
    public void stopTimestampUpdates() {
        updateHandler.removeCallbacks(updateRunnable);
    }
    
    /**
     * Update all visible timestamps
     */
    private void updateAllTimestamps() {
        for (FavoriteViewHolder holder : activeViewHolders) {
            holder.updateTimestamp();
        }
    }
    
    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof FavoriteViewHolder) {
            activeViewHolders.remove((FavoriteViewHolder) holder);
        }
    }
    
    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof FavoriteViewHolder) {
            activeViewHolders.add((FavoriteViewHolder) holder);
        }
    }
    
    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof FavoriteViewHolder) {
            activeViewHolders.remove((FavoriteViewHolder) holder);
        }
    }
    
    // Footer ViewHolder
    class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView ticketMasterTextView;
        
        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            ticketMasterTextView = itemView.findViewById(R.id.ticketMasterTextView);
            
            ticketMasterTextView.setOnClickListener(v -> {
                // Open TicketMaster URL in browser
                String url = context.getString(R.string.ticketmaster_url);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(browserIntent);
            });
        }
        
        public void bind() {
            // Footer doesn't need any binding logic
        }
    }
    
    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImageView;
        TextView eventNameTextView;
        TextView eventDateTextView;
        TextView timeSinceTextView;
        Event currentEvent;
        
        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImageView = itemView.findViewById(R.id.eventImageView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
            timeSinceTextView = itemView.findViewById(R.id.timeSinceTextView);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < events.size() && listener != null) {
                    Event event = events.get(position);
                    if (event != null) {
                        listener.onEventClick(event);
                    }
                }
            });
        }
        
        public void bind(Event event) {
            if (event == null) {
                return;
            }
            
            this.currentEvent = event;
            
            // Load event image (circular)
            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(event.getImageUrl())
                        .centerCrop()
                        .into(eventImageView);
            } else {
                eventImageView.setImageResource(R.drawable.ic_location);
            }
            
            // Set event name (max 1 line with ellipsis)
            String eventName = event.getName();
            eventNameTextView.setText(eventName != null ? eventName : "");
            
            // Set event date with full date format (e.g., "Aug 8, 2026, 5:30 PM")
            String formattedDate = formatEventDateTime(event.getDateTime());
            eventDateTextView.setText(formattedDate != null ? formattedDate : "");
            
            // Set initial time since text based on UTC
            updateTimestamp();
        }
        
        /**
         * Update the timestamp text (called every second)
         */
        public void updateTimestamp() {
            if (currentEvent != null && timeSinceTextView != null) {
                String utc = currentEvent.getUtc();
                if (utc != null && !utc.isEmpty()) {
                    String timeSince = formatTimeSince(utc);
                    timeSinceTextView.setText(timeSince);
                } else {
                    timeSinceTextView.setText("");
                }
            }
        }
        
        /**
         * Format event date/time to "MMM d, yyyy, h:mm a" format
         * @param dateString Date string (e.g., "2026-08-08T17:30:00Z")
         * @return Formatted date string (e.g., "Aug 8, 2026, 5:30 PM")
         */
        private String formatEventDateTime(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "";
            }
            
            try {
                synchronized (INPUT_FORMAT_WITH_TIME) {
                    SimpleDateFormat inputFormat;
                    if (dateString.contains("T")) {
                        // ISO 8601 with time - use static formatter
                        inputFormat = INPUT_FORMAT_WITH_TIME;
                    } else {
                        // Date only - use static formatter
                        synchronized (INPUT_FORMAT_DATE_ONLY) {
                            inputFormat = INPUT_FORMAT_DATE_ONLY;
                        }
                    }
                    
                    Date date = inputFormat.parse(dateString);
                    if (date != null) {
                        synchronized (OUTPUT_FORMAT_EVENT_DATE) {
                            return OUTPUT_FORMAT_EVENT_DATE.format(date);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return dateString;
        }
        
        /**
         * Format the time since the UTC timestamp
         * @param utcString UTC timestamp in ISO 8601 format (e.g., "2025-11-07T03:12:58.865Z")
         * @return Formatted time string
         */
        private String formatTimeSince(String utcString) {
            if (utcString == null || utcString.isEmpty()) {
                return "";
            }
            
            try {
                // Parse UTC timestamp using static formatter
                Date utcDate;
                synchronized (UTC_FORMAT) {
                    utcDate = UTC_FORMAT.parse(utcString);
                }
                
                if (utcDate == null) {
                    return "";
                }
                
                // Get current time
                Date now = new Date();
                long diffMillis = now.getTime() - utcDate.getTime();
                long diffSeconds = diffMillis / 1000;
                long diffMinutes = diffSeconds / 60;
                long diffHours = diffMinutes / 60;
                long diffDays = diffHours / 24;
                
                // Check if it's within today (less than 24 hours)
                if (diffDays < 1) {
                    // Within today
                    if (diffHours >= 1) {
                        // Hours
                        if (diffHours == 1) {
                            return "a hour ago";
                        } else {
                            return diffHours + " hours ago";
                        }
                    } else if (diffMinutes >= 2) {
                        // Multiple minutes
                        return diffMinutes + " minutes ago";
                    } else if (diffMinutes >= 1) {
                        // Between 1 and 2 minutes
                        return "a minute ago";
                    } else {
                        // Seconds
                        if (diffSeconds <= 0) {
                            return "just now";
                        }
                        return diffSeconds + " seconds ago";
                    }
                } else {
                    // More than 24 hours ago - show mm/dd/yy format using static formatter
                    synchronized (DISPLAY_FORMAT) {
                        return DISPLAY_FORMAT.format(utcDate);
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
    }
}

