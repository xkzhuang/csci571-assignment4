package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.adapters.ViewPagerAdapter;
import com.example.myapplication.fragments.FavoritesFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private MaterialButton searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install splash screen (must be called before super.onCreate() on Android 12+)
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        
        super.onCreate(savedInstanceState);
        
        // Enable Dynamic Colors (Material You) - adapts to wallpaper on Android 12+
        DynamicColors.applyToActivityIfAvailable(this);
        
        setContentView(R.layout.activity_main);
        
        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.viewPager);
        searchButton = findViewById(R.id.searchButton);
        
        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }
        
        // Setup search button click listener to launch SearchActivity
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch SearchActivity
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
        
        // Setup ViewPager with only FavoritesFragment
        setupViewPager();
    }
    
    private void setupViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(this);
        
        // Add only FavoritesFragment
        viewPagerAdapter.addFragment(new FavoritesFragment(), getString(R.string.tab_favorites));
        
        viewPager.setAdapter(viewPagerAdapter);
        
        // Disable swipe gesture on ViewPager since there's only one page
        viewPager.setUserInputEnabled(false);
    }
}