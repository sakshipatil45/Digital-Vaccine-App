package com.example.digitalvaccineapp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Display for 4 seconds
        new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginAndRedirect, 4000);
    }

    private void checkLoginAndRedirect() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isLoggedInSharedPrefs = prefs.getBoolean("isLoggedIn", false);
        
        // Also check Firebase just in case
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent;
        if (isLoggedInSharedPrefs || user != null) {
            // Redirect to Dashboard
            intent = new Intent(SplashActivity.this, VaccinationActivity.class);
        } else {
            // Redirect to Login (Assuming MainActivity is acting as the starting/login point, 
            // since there is no explicit LoginActivity right now, or create a basic LoginActivity placeholder)
            intent = new Intent(SplashActivity.this, com.example.digitalvaccineapp.MainActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
