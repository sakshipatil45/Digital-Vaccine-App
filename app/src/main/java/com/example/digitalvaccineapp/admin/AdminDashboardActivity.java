package com.example.digitalvaccineapp.admin;

import com.example.digitalvaccineapp.shared.ProfileActivity;
import com.example.digitalvaccineapp.auth.LoginActivity;
import com.example.digitalvaccineapp.shared.NotificationsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.card.MaterialCardView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalvaccineapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.concurrent.atomic.AtomicInteger;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeAdmin, tvTotalUsers, tvVaccinatedCount;
    private MaterialCardView btnAddVaccine, btnUpdateSchedule, btnSendAlert, btnManageUsers, btnProfile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind Views
        tvWelcomeAdmin = findViewById(R.id.tvWelcomeAdmin);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvVaccinatedCount = findViewById(R.id.tvVaccinatedCount);

        btnAddVaccine = findViewById(R.id.btnAdminAddVaccine);
        btnUpdateSchedule = findViewById(R.id.btnAdminUpdateSchedule);
        btnSendAlert = findViewById(R.id.btnAdminSendAlert);
        btnManageUsers = findViewById(R.id.btnAdminViewUsers);
        btnProfile = findViewById(R.id.btnAdminProfile);

        // Click Listeners
        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
        findViewById(R.id.btnAdminNotifications).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });
        btnAddVaccine.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminVaccineActivity.class));
        });

        btnUpdateSchedule.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.digitalvaccineapp.shared.ReminderActivity.class));
        });

        btnSendAlert.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAnnouncementsActivity.class));
        });

        btnManageUsers.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminUserListActivity.class));
        });

        findViewById(R.id.btnAdminLogoutHeader).setOnClickListener(v -> {
            logout();
        });

        loadAdminProfile();
        setupRealTimeStats();
    }


    private void loadAdminProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        
        db.collection("users").document(user.getUid())
            .addSnapshotListener((document, e) -> {
                if (e != null || document == null || !document.exists()) return;
                String name = document.getString("name");
                if (name != null && !name.isEmpty()) {
                    tvWelcomeAdmin.setText("Hello, " + name);
                } else {
                    tvWelcomeAdmin.setText("Hello, Admin");
                }
            });
    }

    private void setupRealTimeStats() {
        // Real-time listener for Total Beneficiaries
        db.collection("beneficiaries")
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null) return;
                
                int totalUsers = snapshots.size();
                tvTotalUsers.setText(String.valueOf(totalUsers));
                
                // Fetch Vaccinated count using Collection Group Query (Real-time)
                db.collectionGroup("vaccinations")
                    .whereEqualTo("status", "Completed")
                    .addSnapshotListener((vaxSnapshots, vaxE) -> {
                        if (vaxE != null || vaxSnapshots == null) return;
                        
                        // We count distinct beneficiary IDs that have at least one completed vaccination
                        // For a simple dashboard, we can just show total doses given or estimate
                        // Let's count unique beneficiaries in this list
                        java.util.Set<String> vaccinatedSet = new java.util.HashSet<>();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : vaxSnapshots) {
                            // The path is beneficiaries/{id}/vaccinations/{vId}
                            String path = doc.getReference().getPath();
                            String[] parts = path.split("/");
                            if (parts.length >= 2) {
                                vaccinatedSet.add(parts[1]);
                            }
                        }
                        
                        int vaccinatedCount = vaccinatedSet.size();
                        tvVaccinatedCount.setText(String.valueOf(vaccinatedCount));
                    });
            });
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Stats are updated via SnapshotListeners (Real-time)
    }
}

