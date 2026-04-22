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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

        findViewById(R.id.btnNotifications).setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.digitalvaccineapp.shared.NotificationsActivity.class));
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_schedule) {
                startActivity(new Intent(this, VaccinationScheduleActivity.class));
                return true;
            }
            if (id == R.id.nav_vaccines) {
                startActivity(new Intent(this, RecordsActivity.class));
                return true;
            }
            if (id == R.id.nav_reminders) {
                startActivity(new Intent(this, ReminderActivity.class));
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
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
        
        // 1. Get Beneficiary Category first
        db.collection("beneficiaries").document(bId).get().addOnSuccessListener(beneficiaryDoc -> {
            String category = beneficiaryDoc.getString("category");
            
            // 2. Fetch History
            db.collection("beneficiaries").document(bId).collection("vaccinations").get()
                .addOnSuccessListener(vaxSnapshots -> {
                    List<Vaccination> history = new ArrayList<>();
                    for (QueryDocumentSnapshot vaxDoc : vaxSnapshots) {
                        history.add(vaxDoc.toObject(Vaccination.class));
                    }
                    
                    // 3. Fetch Dynamic Due Vaccines (Master Schedule - History)
                    repository.getDueVaccines(bId, category, new VaccinationRepository.DataCallback() {
                        @Override
                        public void onDataLoaded(List<Vaccination> dueVaccines) {
                            // 4. Fetch Targeted Campaigns for this category
                            db.collection("campaign_reminders")
                                .whereEqualTo("targetCategory", category)
                                .get()
                                .addOnSuccessListener(campaignSnapshots -> {
                                    int campaignCount = campaignSnapshots.size();
                                    int totalDueCount = (dueVaccines != null ? dueVaccines.size() : 0) + campaignCount;
                                    updateSummary(history, totalDueCount);
                                    
                                    if (campaignCount > 0) {
                                        // Show latest campaign in announcement card
                                        QueryDocumentSnapshot lastCampaign = (QueryDocumentSnapshot) campaignSnapshots.getDocuments().get(campaignSnapshots.size() - 1);
                                        showCampaignAlert(lastCampaign.getString("vaccineName"), lastCampaign.getString("reminderDate"));
                                    }
                                });
                        }
                        @Override public void onError(String msg) { 
                            updateSummary(history, 0);
                        }
                    });
                });
        });
    }

    private void showCampaignAlert(String name, String date) {
        findViewById(R.id.cardLatestAnnouncement).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.tvAnnounceTitle)).setText("🚨 Group Alert: " + name);
        ((TextView) findViewById(R.id.tvAnnounceMsg)).setText("Special vaccination drive scheduled for " + date + ". Please visit your nearest center.");
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
        List<Vaccination> allHistory = new ArrayList<>();
        AtomicInteger totalDynamicDue = new AtomicInteger(0);
        
        db.collection("beneficiaries").whereEqualTo("mobileNumber", phone).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    updateSummary(allHistory, 0);
                    return;
                }

                int totalMembers = queryDocumentSnapshots.size();
                AtomicInteger processed = new AtomicInteger(0);

                for (QueryDocumentSnapshot memberDoc : queryDocumentSnapshots) {
                    String bId = memberDoc.getId();
                    String category = memberDoc.getString("category");
                    
                    // 1. Fetch History
                    db.collection("beneficiaries").document(bId).collection("vaccinations").get()
                        .addOnSuccessListener(vaxSnapshots -> {
                            for (QueryDocumentSnapshot vaxDoc : vaxSnapshots) {
                                allHistory.add(vaxDoc.toObject(Vaccination.class));
                            }
                            
                            // 2. Fetch Dynamic Due for this member
                            repository.getDueVaccines(bId, category, new VaccinationRepository.DataCallback() {
                                @Override
                                public void onDataLoaded(List<Vaccination> dueVaccines) {
                                    if (dueVaccines != null) {
                                        totalDynamicDue.addAndGet(dueVaccines.size());
                                    }

                                    // 3. Fetch Campaigns for this category
                                    db.collection("campaign_reminders")
                                        .whereEqualTo("targetCategory", category)
                                        .get()
                                        .addOnSuccessListener(campaignSnapshots -> {
                                            totalDynamicDue.addAndGet(campaignSnapshots.size());
                                            
                                            if (processed.incrementAndGet() == totalMembers) {
                                                runOnUiThread(() -> updateSummary(allHistory, totalDynamicDue.get()));
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            if (processed.incrementAndGet() == totalMembers) {
                                                runOnUiThread(() -> updateSummary(allHistory, totalDynamicDue.get()));
                                            }
                                        });
                                }
                                @Override public void onError(String msg) {
                                    if (processed.incrementAndGet() == totalMembers) {
                                        runOnUiThread(() -> updateSummary(allHistory, totalDynamicDue.get()));
                                    }
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
