package com.example.myapplication;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Glide module for optimizing image loading performance and memory usage
 */
@GlideModule
public class MyGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Set memory cache size to 20MB (default is 10MB)
        // This helps with performance when loading many images
        int memoryCacheSizeBytes = 20 * 1024 * 1024; // 20 MB
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
        
        // Set disk cache size to 100MB (default is 250MB, but we reduce for better memory management)
        int diskCacheSizeBytes = 100 * 1024 * 1024; // 100 MB
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
    }
    
    @Override
    public boolean isManifestParsingEnabled() {
        // Disable manifest parsing for better performance
        return false;
    }
}

