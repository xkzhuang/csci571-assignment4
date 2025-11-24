package com.example.myapplication;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

/**
 * Application class to apply Material Design 3 dynamic colors globally.
 * Dynamic colors adapt to the user's wallpaper on Android 12+ (API 31+).
 */
public class MyApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Apply dynamic colors globally to all activities
        // This will adapt the app's color scheme to the user's wallpaper on Android 12+
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}

