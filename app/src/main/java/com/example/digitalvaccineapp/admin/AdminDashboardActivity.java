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
    private MaterialCardView btnAddUser, btnAddFamily, btnUpdateSchedule, btnSendAlert;
    private BottomNavigationView bottomNavigationView;

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

        com.google.android.material.card.MaterialCardView btnScheduledList = findViewById(R.id.btnAdminScheduledList);
        btnAddFamily = findViewById(R.id.btnAdminAddFamily);
        btnUpdateSchedule = findViewById(R.id.btnAdminUpdateSchedule);
        btnSendAlert = findViewById(R.id.btnAdminSendAlert);
        bottomNavigationView = findViewById(R.id.adminBottomNav);

        // Quick Action Click Listeners
        btnScheduledList.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminScheduledCampaignsActivity.class));
        });

        btnAddFamily.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.digitalvaccineapp.citizen.AddFamilyMemberActivity.class));
        });

        btnUpdateSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.digitalvaccineapp.shared.ReminderActivity.class);
            intent.putExtra("isAdmin", true);
            startActivity(intent);
        });

        btnSendAlert.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAnnouncementsActivity.class));
        });

        findViewById(R.id.btnAdminNotifications).setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.digitalvaccineapp.shared.NotificationsActivity.class));
        });

        findViewById(R.id.btnAdminLogoutHeader).setOnClickListener(v -> {
            logout();
        });

        // Bottom Navigation Setup
        bottomNavigationView.setSelectedItemId(R.id.nav_admin_dash);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_dash) {
                return true;
            } else if (id == R.id.nav_admin_vaccines) {
                startActivity(new Intent(this, AdminVaccineActivity.class));
                return false;
            } else if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUserListActivity.class));
                return false;
            } else if (id == R.id.nav_admin_profile) {
                startActivity(new Intent(this, AdminProfileActivity.class));
                return false;
            }
            return false;
        });

        loadAdminProfile();
        setupRealTimeStats();
    }

    private void loadAdminProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            return;

        db.collection("users").document(user.getUid())
                .addSnapshotListener((document, e) -> {
                    if (e != null || document == null || !document.exists())
                        return;
                    String name = document.getString("name");
                    if (name != null && !name.isEmpty()) {
                        tvWelcomeAdmin.setText("Hello, " + name);
                    } else {
                        tvWelcomeAdmin.setText("Hello, Admin");
                    }
                });
    }

    private void setupRealTimeStats() {
        // Real-time listener for Total Beneficiaries (Family Members)
        db.collection("family_members")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null)
                        return;

                    int totalUsers = snapshots.size();
                    tvTotalUsers.setText(String.valueOf(totalUsers));

                    // Fetch Vaccinated count from flattened vaccinations collection
                    db.collection("vaccinations")
                            .whereEqualTo("status", "Completed")
                            .addSnapshotListener((vaxSnapshots, vaxE) -> {
                                if (vaxE != null || vaxSnapshots == null)
                                    return;

                                java.util.Set<String> vaccinatedSet = new java.util.HashSet<>();
                                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : vaxSnapshots) {
                                    String memberId = doc.getString("memberId");
                                    if (memberId != null) {
                                        vaccinatedSet.add(memberId);
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
