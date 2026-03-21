package com.example.digitalvaccineapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.adapter.VaccinationAdapter;
import com.example.digitalvaccineapp.models.Vaccination;
import com.example.digitalvaccineapp.network.VaccinationRepository;
import com.example.digitalvaccineapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class VaccinationActivity extends AppCompatActivity {
    private TextView tvWelcomeName;
    private VaccinationRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccination);

        repository = new VaccinationRepository(this);
        tvWelcomeName = findViewById(R.id.tvWelcomeName);

        // Fetch user name and dashboard counts
        loadDashboardData();

        // 1. Add Vaccine Button
        findViewById(R.id.btnDashAddVaccine).setOnClickListener(v -> {
            startActivity(new Intent(this, AddVaccinationActivity.class));
        });

        // 2. View Records Button
        findViewById(R.id.btnDashViewRecords).setOnClickListener(v -> {
            startActivity(new Intent(this, RecordsActivity.class));
        });

        // 3. Reminders Button
        findViewById(R.id.btnDashReminders).setOnClickListener(v -> {
            startActivity(new Intent(this, ReminderActivity.class));
        });

        // 4. Vaccine Info Button
        findViewById(R.id.btnDashVaccineInfo).setOnClickListener(v -> {
            startActivity(new Intent(this, VaccineInfoActivity.class));
        });

        // 5. Profile Button
        findViewById(R.id.btnDashProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
        
        // 6. Family Members Button
        findViewById(R.id.btnDashFamily).setOnClickListener(v -> {
            startActivity(new Intent(this, FamilyMembersActivity.class));
        });
        
        // 7. Certificate Button
        findViewById(R.id.btnDashCertificate).setOnClickListener(v -> {
            startActivity(new Intent(this, CertificateActivity.class));
        });

        // 6. Logout Button
        findViewById(R.id.btnDashLogout).setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void loadDashboardData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) return;

        // Real-time listener for Profile Name
        db.collection("users").document(mAuth.getCurrentUser().getUid())
            .addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) return;
                
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    if (name != null && !name.isEmpty()) {
                        tvWelcomeName.setText("Hello, " + name + " 👋");
                    } else {
                        tvWelcomeName.setText("Hello, " + mAuth.getCurrentUser().getEmail().split("@")[0] + " 👋");
                    }
                }
            });

        // Fetch Vaccine counts
        repository.getVaccinations(new VaccinationRepository.DataCallback() {
            @Override
            public void onDataLoaded(List<Vaccination> vaccinations) {
                runOnUiThread(() -> {
                    updateSummary(vaccinations);
                });
            }

            @Override
            public void onError(String message) {
                // Handle error
            }
        });
    }

    private void updateSummary(List<Vaccination> vaccinationList) {
        if (vaccinationList == null) return;
        int completed = 0;
        int pending = 0;
        for (Vaccination v : vaccinationList) {
            String status = v.getStatus() != null ? v.getStatus().toLowerCase() : "pending";
            if (status.contains("completed") || status.contains("done")) {
                completed++;
            } else {
                pending++;
            }
        }
        
        TextView tvCompleted = findViewById(R.id.tvCompletedCount);
        TextView tvPending = findViewById(R.id.tvPendingCount);
        TextView tvProgressPercent = findViewById(R.id.tvProgressPercent);
        com.google.android.material.progressindicator.LinearProgressIndicator pbProgress = findViewById(R.id.pbVaccinationProgress);
        
        tvCompleted.setText(String.valueOf(completed));
        tvPending.setText(String.valueOf(pending));

        int total = completed + pending;
        int progress = total > 0 ? (completed * 100) / total : 0;
        
        pbProgress.setProgress(progress, true);
        tvProgressPercent.setText(progress + "%");
    }

    private void logoutUser() {
        // Clear SharedPreferences
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().clear().apply();
        
        // Sign out of Firebase
        FirebaseAuth.getInstance().signOut();
        
        // Redirect to Login/Welcome
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData(); // Refresh counts from local Room DB
    }
}
