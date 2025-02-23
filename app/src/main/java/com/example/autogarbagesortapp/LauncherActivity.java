package com.example.autogarbagesortapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher); // Set the splash screen layout

        // Delay for a short time (e.g., 2 seconds) to show the splash screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if the user is logged in
                boolean isLoggedIn = isLoggedIn();

                // Navigate to the appropriate activity
                Intent intent;
                if (isLoggedIn) {
                    intent = new Intent(LauncherActivity.this, MainActivity.class);
                } else {
                    intent = new Intent(LauncherActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish(); // Close the LauncherActivity
            }
        }, 2000); // 2000 milliseconds = 2 seconds
    }

    private boolean isLoggedIn() {
        // Retrieve the login status from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("is_logged_in", false); // Default to false if not set
    }
}