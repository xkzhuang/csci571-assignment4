package com.example.myapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.SpotifyAlbum;

import java.util.ArrayList;
import java.util.List;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder> {
    
    private List<SpotifyAlbum> albums;
    private Context context;
    
    public AlbumsAdapter(Context context) {
        this.context = context;
        this.albums = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album_card, parent, false);
        return new AlbumViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        SpotifyAlbum album = albums.get(position);
        holder.bind(album);
    }
    
    @Override
    public int getItemCount() {
        return albums.size();
    }
    
    public void setAlbums(List<SpotifyAlbum> albums) {
        this.albums = albums != null ? albums : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView albumCoverImageView;
        TextView albumNameTextView;
        TextView releaseDateTextView;
        TextView trackCountTextView;
        
        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumCoverImageView = itemView.findViewById(R.id.albumCoverImageView);
            albumNameTextView = itemView.findViewById(R.id.albumNameTextView);
            releaseDateTextView = itemView.findViewById(R.id.releaseDateTextView);
            trackCountTextView = itemView.findViewById(R.id.trackCountTextView);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    SpotifyAlbum album = albums.get(position);
                    if (album.getSpotifyUrl() != null && !album.getSpotifyUrl().isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(album.getSpotifyUrl()));
                        context.startActivity(intent);
                    }
                }
            });
        }
        
        public void bind(SpotifyAlbum album) {
            // Album cover
            if (album.getImageUrl() != null && !album.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(album.getImageUrl())
                        .centerCrop()
                        .into(albumCoverImageView);
            } else {
                albumCoverImageView.setImageResource(R.drawable.ic_location);
            }
            
            // Album name
            albumNameTextView.setText(album.getName());
            
            // Release date
            releaseDateTextView.setText(album.getReleaseDate());
            
            // Track count
            int trackCount = album.getTotalTracks();
            String trackText = trackCount == 1 ? "1 track" : trackCount + " tracks";
            trackCountTextView.setText(trackText);
        }
    }
}

