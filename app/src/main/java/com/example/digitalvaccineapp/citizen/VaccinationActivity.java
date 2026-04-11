package com.example.digitalvaccineapp.citizen;


import com.example.digitalvaccineapp.shared.ProfileActivity;
import com.example.digitalvaccineapp.shared.AddVaccinationActivity;
import com.example.digitalvaccineapp.shared.RecordsActivity;
import com.example.digitalvaccineapp.auth.WelcomeActivity;
import com.example.digitalvaccineapp.shared.ReminderActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.shared.VaccinationAdapter;
import com.example.digitalvaccineapp.shared.Vaccination;
import com.example.digitalvaccineapp.network.VaccinationRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

        // 2. View Records Button
        findViewById(R.id.btnDashViewRecords).setOnClickListener(v -> {
            startActivity(new Intent(this, RecordsActivity.class));
        });

        // 3. Reminders Button
        findViewById(R.id.btnDashReminders).setOnClickListener(v -> {
            startActivity(new Intent(this, ReminderActivity.class));
        });

        // 5. Profile Button
        findViewById(R.id.btnDashProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        findViewById(R.id.btnNotifications).setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.digitalvaccineapp.shared.NotificationsActivity.class));
        });

        // 6. Family Members Button
        findViewById(R.id.btnDashFamily).setOnClickListener(v -> {
            startActivity(new Intent(this, FamilyMembersActivity.class));
        });

        // 6. Logout Button
        findViewById(R.id.btnDashLogout).setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void loadDashboardData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Real-time listener for Profile Name
        db.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) return;

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        tvWelcomeName.setText("Hello, " + (name != null ? name : "User"));
                        
                        String phone = documentSnapshot.getString("phone");
                        if (phone != null && !phone.isEmpty()) {
                            aggregateVaccinationsForFamily(phone);
                        }
                    }
                });
    }

    private void aggregateVaccinationsForFamily(String phone) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Vaccination> allVaccinations = new ArrayList<>();
        
        // 1. Find all beneficiaries linked by phone
        db.collection("beneficiaries").whereEqualTo("mobileNumber", phone).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    updateSummary(allVaccinations);
                    return;
                }

                int totalMembers = queryDocumentSnapshots.size();
                AtomicInteger processed = new AtomicInteger(0);

                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    // 2. For each member, fetch their sub-collection vaccinations
                    db.collection("beneficiaries").document(doc.getId()).collection("vaccinations").get()
                        .addOnSuccessListener(vaxSnapshots -> {
                            for (QueryDocumentSnapshot vaxDoc : vaxSnapshots) {
                                Vaccination v = vaxDoc.toObject(Vaccination.class);
                                allVaccinations.add(v);
                            }
                            
                            if (processed.incrementAndGet() == totalMembers) {
                                runOnUiThread(() -> updateSummary(allVaccinations));
                            }
                        });
                }
            });
    }

    private void updateSummary(List<Vaccination> vaccinationList) {
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
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().clear().apply();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }
}
