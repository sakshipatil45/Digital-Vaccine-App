package com.example.digitalvaccineapp.auth;

import com.example.digitalvaccineapp.citizen.VaccinationActivity;
import com.example.digitalvaccineapp.admin.AdminDashboardActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalvaccineapp.R;
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
            String role = prefs.getString("userRole", "citizen");
            if ("admin".equals(role)) {
                intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, VaccinationActivity.class);
            }
        } else {
            // Redirect to Welcome / Landing Screen
            intent = new Intent(SplashActivity.this, WelcomeActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
