package com.example.digitalvaccineapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminProfileActivity extends AppCompatActivity {
    private TextView tvName, tvEmail;
    private MaterialCardView cardEditProfile, cardLogout;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        

        cardEditProfile = findViewById(R.id.cardEditProfile);
        cardLogout = findViewById(R.id.cardLogout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            tvEmail.setText(mAuth.getCurrentUser().getEmail());
        }

        cardEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(AdminProfileActivity.this, AdminEditProfileActivity.class));
        });

        cardLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            // We should ideally navigate to LoginActivity and clear the top
            Intent intent = new Intent(AdminProfileActivity.this, com.example.digitalvaccineapp.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Bottom Navigation Setup
        bottomNavigationView = findViewById(R.id.adminBottomNav);
        bottomNavigationView.setSelectedItemId(R.id.nav_admin_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_dash) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return false;
            } else if (id == R.id.nav_admin_vaccines) {
                startActivity(new Intent(this, AdminVaccineActivity.class));
                return false;
            } else if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUserListActivity.class));
                return false;
            } else if (id == R.id.nav_admin_profile) {
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fetch profile every time user comes back to this screen
        fetchProfile();
    }

    private void fetchProfile() {
        if (mAuth.getCurrentUser() == null) return;
        
        db.collection("users").document(mAuth.getCurrentUser().getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    com.example.digitalvaccineapp.shared.User user = documentSnapshot.toObject(com.example.digitalvaccineapp.shared.User.class);
                    if (user != null) {
                        tvName.setText(user.getName() != null && !user.getName().isEmpty() ? user.getName() : "User");
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(AdminProfileActivity.this, "Error fetching profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
