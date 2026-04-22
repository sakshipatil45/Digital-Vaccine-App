package com.example.digitalvaccineapp.admin;

import com.example.digitalvaccineapp.shared.ReminderActivity;
import com.example.digitalvaccineapp.shared.ProfileActivity;
import com.example.digitalvaccineapp.auth.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.digitalvaccineapp.shared.NotificationsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalvaccineapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeAdmin, tvTotalBeneficiaries, tvOverdueAlerts;
    private ImageButton btnProfileAdmin;
    private MaterialButton btnAddBeneficiary, btnViewRecords, btnReminders, btnAlerts, btnReports, btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvWelcomeAdmin = findViewById(R.id.tvWelcomeAdmin);
        tvTotalBeneficiaries = findViewById(R.id.tvTotalBeneficiaries);
        tvOverdueAlerts = findViewById(R.id.tvOverdueAlerts);
        btnProfileAdmin = findViewById(R.id.btnProfileAdmin);

        btnAddBeneficiary = findViewById(R.id.btnAdminAddBeneficiary);
        btnViewRecords = findViewById(R.id.btnAdminViewRecords);

        btnAlerts = findViewById(R.id.btnAdminAlerts);
        btnLogout = findViewById(R.id.btnAdminLogout);

        loadAdminProfile();
        loadDashboardStats();

        findViewById(R.id.btnNotificationsAdmin).setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, NotificationsActivity.class));
        });

        findViewById(R.id.btnAdminAddBeneficiary).setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminAddUserActivity.class));
        });

        findViewById(R.id.btnAdminVaccineInventory).setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminVaccineActivity.class));
        });

        findViewById(R.id.btnAdminAnnouncements).setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminAnnouncementsActivity.class));
        });

        findViewById(R.id.btnAdminReports).setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminReportsActivity.class));
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation_admin);
        nav.setSelectedItemId(R.id.nav_admin_dash);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_dash) return true;
            if (id == R.id.nav_admin_vaccines) {
                startActivity(new Intent(this, AdminVaccineActivity.class));
                return true;
            }
            if (id == R.id.nav_admin_users) {
                startActivity(new Intent(this, AdminUserListActivity.class));
                return true;
            }
            if (id == R.id.nav_admin_notifications) {
                startActivity(new Intent(this, AdminAnnouncementsActivity.class));
                return true;
            }
            if (id == R.id.nav_admin_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }

    private void loadAdminProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        db.collection("users").document(user.getUid()).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    String name = document.getString("name");
                    if (name != null) {
                        tvWelcomeAdmin.setText("Welcome, " + name + " (Admin) 🛠️");
                    }
                }
            });
    }

    private void loadDashboardStats() {
        // Admin sees GLOBAL stats now
        db.collection("beneficiaries")
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null) return;
                
                int totalCount = snapshots.size();
                tvTotalBeneficiaries.setText(String.valueOf(totalCount));
                
                // Fetch actual overdue count from global record search if needed
                // For now, using a placeholder logic similar to before but global
                tvOverdueAlerts.setText(String.valueOf(totalCount / 3)); 
            });
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Admin logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
