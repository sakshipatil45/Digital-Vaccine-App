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
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.view.View;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class VaccinationActivity extends AppCompatActivity {
    private TextView tvWelcomeName;
    private Spinner spinnerProfileSwitch;
    private VaccinationRepository repository;
    private List<String> memberNames = new ArrayList<>();
    private Map<String, String> memberIdMap = new HashMap<>(); // Name -> ID
    private String selectedMemberId = null; // null means "All Family"
    private String userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccination);

        repository = new VaccinationRepository(this);
        tvWelcomeName = findViewById(R.id.tvWelcomeName);
        spinnerProfileSwitch = findViewById(R.id.spinnerProfileSwitch);

        // Fetch user name and dashboard counts
        loadDashboardData();
        loadLatestAnnouncement();

        findViewById(R.id.btnDashSchedule).setOnClickListener(v -> {
            startActivity(new Intent(this, VaccinationScheduleActivity.class));
        });

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
        
        db.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) return;

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        tvWelcomeName.setText("Hello, " + (name != null ? name : "User"));
                        
                        userPhone = documentSnapshot.getString("phone");
                        if (userPhone != null && !userPhone.isEmpty()) {
                            setupProfileSwitcher(userPhone);
                            if (selectedMemberId == null) {
                                aggregateDataForFamily(userPhone);
                            }
                        }
                    }
                });
    }

    private void setupProfileSwitcher(String phone) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("beneficiaries").whereEqualTo("mobileNumber", phone).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                memberNames.clear();
                memberIdMap.clear();
                memberNames.add("All Family Members");
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String name = doc.getString("name");
                    if (name != null) {
                        memberNames.add(name);
                        memberIdMap.put(name, doc.getId());
                    }
                }
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, memberNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerProfileSwitch.setAdapter(adapter);
                
                spinnerProfileSwitch.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            selectedMemberId = null;
                            aggregateDataForFamily(phone);
                        } else {
                            String name = memberNames.get(position);
                            selectedMemberId = memberIdMap.get(name);
                            fetchDataForSingleMember(selectedMemberId);
                        }
                    }
                    @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
            });
    }

    private void fetchDataForSingleMember(String bId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Vaccination> vaxList = new ArrayList<>();
        
        db.collection("beneficiaries").document(bId).collection("vaccinations").get()
            .addOnSuccessListener(vaxSnapshots -> {
                for (QueryDocumentSnapshot vaxDoc : vaxSnapshots) {
                    vaxList.add(vaxDoc.toObject(Vaccination.class));
                }
                
                db.collection("beneficiaries").document(bId).collection("reminders").get()
                    .addOnSuccessListener(remSnapshots -> {
                        updateSummary(vaxList, remSnapshots.size());
                    });
            });
    }

    private void loadLatestAnnouncement() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("announcements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null || snapshots.isEmpty()) return;
                
                QueryDocumentSnapshot doc = (QueryDocumentSnapshot) snapshots.getDocuments().get(0);
                findViewById(R.id.cardLatestAnnouncement).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.tvAnnounceTitle)).setText(doc.getString("title"));
                ((TextView) findViewById(R.id.tvAnnounceMsg)).setText(doc.getString("message"));
            });
    }

    private void aggregateDataForFamily(String phone) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Vaccination> allVaccinations = new ArrayList<>();
        AtomicInteger totalReminders = new AtomicInteger(0);
        
        // 1. Find all beneficiaries linked by mobileNumber
        db.collection("beneficiaries").whereEqualTo("mobileNumber", phone).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    updateSummary(allVaccinations, totalReminders.get());
                    return;
                }

                int totalMembers = queryDocumentSnapshots.size();
                AtomicInteger processed = new AtomicInteger(0);

                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String bId = doc.getId();
                    
                    // 2. Fetch Completed/Pending Vaccinations
                    db.collection("beneficiaries").document(bId).collection("vaccinations").get()
                        .addOnSuccessListener(vaxSnapshots -> {
                            for (QueryDocumentSnapshot vaxDoc : vaxSnapshots) {
                                Vaccination v = vaxDoc.toObject(Vaccination.class);
                                allVaccinations.add(v);
                            }
                            
                            // 3. Fetch Shared Reminders (counts as extra pending)
                            db.collection("beneficiaries").document(bId).collection("reminders").get()
                                .addOnSuccessListener(remSnapshots -> {
                                    totalReminders.addAndGet(remSnapshots.size());
                                    
                                    if (processed.incrementAndGet() == totalMembers) {
                                        runOnUiThread(() -> updateSummary(allVaccinations, totalReminders.get()));
                                    }
                                });
                        });
                }
            });
    }

    private void updateSummary(List<Vaccination> vaccinationList, int reminderCount) {
        int completed = 0;
        int pending = reminderCount; // Reminders are strictly pending things to do
        
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
