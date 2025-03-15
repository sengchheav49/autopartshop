package com.example.autopartsshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.autopartsshop.R;
import com.example.autopartsshop.utils.SharedPrefManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        ImageView imageViewLogo = findViewById(R.id.imageViewLogo);

        // Create fade-in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);

        // Set animation listener
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Not needed
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Start a delayed handler to navigate to the next screen
                new Handler(Looper.getMainLooper()).postDelayed(() -> navigateNext(), SPLASH_DURATION);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Not needed
            }
        });

        // Apply animation to views
        imageViewLogo.startAnimation(fadeIn);
    }

    /**
     * Navigates to the appropriate screen based on login status
     */
    private void navigateNext() {
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);

        // Check if the user is already logged in
        if (sharedPrefManager.isLoggedIn()) {
            // Check if admin or regular user
            if (sharedPrefManager.isAdmin()) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
        } else {
            // Navigate to login screen
            startActivity(new Intent(this, LoginActivity.class));
        }

        // Close this activity
        finish();
    }
}