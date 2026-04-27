package com.example.digitalvaccineapp.citizen;

import com.example.digitalvaccineapp.shared.ViewRemindersActivity;
import com.example.digitalvaccineapp.shared.ProfileActivity;
import com.example.digitalvaccineapp.shared.RecordsActivity;
import com.example.digitalvaccineapp.auth.WelcomeActivity;
import com.example.digitalvaccineapp.shared.ReminderActivity;
import com.example.digitalvaccineapp.shared.NotificationsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.shared.Vaccination;
import com.example.digitalvaccineapp.network.VaccinationRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.card.MaterialCardView;
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
    private TextView tvWelcomeName, tvCompletedCount, tvSelectedMemberName, tvSelectedMemberInfo;
    private MaterialCardView btnDashProfileCard, btnDashViewRecords, btnDashReminders, btnDashFamily;
    private BottomNavigationView bottomNavigationView;

    private VaccinationRepository repository;
    private List<String> memberNames = new ArrayList<>();
    private Map<String, String> memberIdMap = new HashMap<>(); 
    private Map<String, String> memberInfoMap = new HashMap<>(); 
    private String selectedMemberId = null; 
    private String userPhone;

    private com.google.firebase.firestore.ListenerRegistration userListener, memberListener, summaryListener, reminderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccination);

        repository = new VaccinationRepository(this);
        
        // Bind Views
        tvWelcomeName = findViewById(R.id.tvWelcomeName);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        
        tvSelectedMemberName = findViewById(R.id.tvSelectedMemberName);
        tvSelectedMemberInfo = findViewById(R.id.tvSelectedMemberInfo);
        MaterialCardView btnChangeMember = findViewById(R.id.btnChangeMember);

        btnChangeMember.setOnClickListener(v -> {
            showMemberSelectionDialog();
        });

        btnDashProfileCard = findViewById(R.id.btnDashProfileCard);
        btnDashViewRecords = findViewById(R.id.btnDashVaccines); // Updated ID
        btnDashReminders = findViewById(R.id.btnDashReminders);
        btnDashFamily = findViewById(R.id.btnDashFamily);
        bottomNavigationView = findViewById(R.id.userBottomNav);
        
        findViewById(R.id.btnNotifications).setOnClickListener(v -> {
            startActivity(new Intent(this, ViewRemindersActivity.class));
        });

        // Use the hidden logic views or existing header buttons
        View logoutBtn = findViewById(R.id.btnDashLogout);
        if (logoutBtn != null) logoutBtn.setOnClickListener(v -> logoutUser());
        
        btnDashProfileCard.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        btnDashViewRecords.setOnClickListener(v -> {
            startActivity(new Intent(this, RecordsActivity.class));
        });

        btnDashReminders.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewRemindersActivity.class);
            intent.putExtra("beneficiaryId", selectedMemberId);
            startActivity(intent);
        });

        btnDashFamily.setOnClickListener(v -> {
            startActivity(new Intent(this, FamilyMembersActivity.class));
        });

        // Bottom Navigation Setup
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_vaccines) {
                startActivity(new Intent(this, RecordsActivity.class));
                return false;
            } else if (id == R.id.nav_reminders) {
                startActivity(new Intent(this, ViewRemindersActivity.class));
                return false;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return false;
            }
            return false;
        });

        loadDashboardData();
    }


    private void loadDashboardData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (userListener != null) userListener.remove();
        
        userListener = db.collection("users").document(user.getUid())
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null || documentSnapshot == null || !documentSnapshot.exists()) return;

                    String name = documentSnapshot.getString("name");
                    tvWelcomeName.setText("Hello, " + (name != null ? name : "User"));
                    
                    userPhone = documentSnapshot.getString("phone");
                    if (userPhone != null && !userPhone.isEmpty()) {
                        setupProfileSwitcher(userPhone);
                    }
                });
    }

    private void setupProfileSwitcher(String phone) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (memberListener != null) memberListener.remove();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        memberListener = db.collection("family_members").whereEqualTo("userId", user.getUid())
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null || queryDocumentSnapshots == null) return;

                memberNames.clear();
                memberIdMap.clear();
                memberInfoMap.clear();
                
                memberNames.add("All Family Members");
                memberInfoMap.put("All Family Members", "Overall family health summary");
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String name = doc.getString("name");
                    String category = doc.getString("category");
                    String age = doc.getString("age");
                    if (name != null) {
                        memberNames.add(name);
                        memberIdMap.put(name, doc.getId());
                        memberInfoMap.put(name, (category != null ? category : "Member") + " | Age " + (age != null ? age : "N/A"));
                    }
                }

                if (selectedMemberId == null && !memberNames.isEmpty()) {
                    updateDashboardForMember(0);
                }
            });
    }

    private void showMemberSelectionDialog() {
        if (memberNames.isEmpty()) return;
        
        String[] namesArray = memberNames.toArray(new String[0]);
        new android.app.AlertDialog.Builder(this)
            .setTitle("Select Member")
            .setItems(namesArray, (dialog, which) -> {
                updateDashboardForMember(which);
            })
            .show();
    }

    private void updateDashboardForMember(int position) {
        String name = memberNames.get(position);
        tvSelectedMemberName.setText(name);
        tvSelectedMemberInfo.setText(memberInfoMap.get(name));

        if (position == 0) {
            selectedMemberId = null;
            aggregateDataForFamily(userPhone);
            if (reminderListener != null) reminderListener.remove();
        } else {
            selectedMemberId = memberIdMap.get(name);
            fetchDataForSingleMember(selectedMemberId);
        }
    }

    private void fetchDataForSingleMember(String bId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (summaryListener != null) summaryListener.remove();

        summaryListener = db.collection("vaccinations").whereEqualTo("memberId", bId)
            .addSnapshotListener((vaxSnapshots, e) -> {
                if (e != null || vaxSnapshots == null) return;

                List<Vaccination> history = new ArrayList<>();
                for (QueryDocumentSnapshot vaxDoc : vaxSnapshots) {
                    history.add(vaxDoc.toObject(Vaccination.class));
                }

                db.collection("family_members").document(bId).get().addOnSuccessListener(beneficiaryDoc -> {
                    String category = beneficiaryDoc.getString("category");
                    repository.getDueVaccines(bId, category, new VaccinationRepository.DataCallback() {
                        @Override
                        public void onDataLoaded(List<Vaccination> dueVaccines) {
                            db.collection("campaign_reminders")
                                .whereEqualTo("targetCategory", category)
                                .get()
                                .addOnSuccessListener(campaignSnapshots -> {
                                    int totalDueCount = (dueVaccines != null ? dueVaccines.size() : 0) + campaignSnapshots.size();
                                    updateSummary(history, totalDueCount);
                                });
                        }
                        @Override public void onError(String msg) { updateSummary(history, 0); }
                    });
                });
            });
    }

    private void aggregateDataForFamily(String phone) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (summaryListener != null) summaryListener.remove();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        summaryListener = db.collection("family_members").whereEqualTo("userId", user.getUid())
            .addSnapshotListener((beneficiaries, e) -> {
                if (e != null || beneficiaries == null) return;
                
                List<Vaccination> allHistory = new ArrayList<>();
                AtomicInteger totalDynamicDue = new AtomicInteger(0);
                int totalMembers = beneficiaries.size();
                if (totalMembers == 0) {
                    updateSummary(allHistory, 0);
                    return;
                }

                AtomicInteger processed = new AtomicInteger(0);
                for (QueryDocumentSnapshot memberDoc : beneficiaries) {
                    String bId = memberDoc.getId();
                    String category = memberDoc.getString("category");

                    db.collection("vaccinations").whereEqualTo("memberId", bId).get()
                        .addOnSuccessListener(vaxSnapshots -> {
                            for (QueryDocumentSnapshot vaxDoc : vaxSnapshots) {
                                allHistory.add(vaxDoc.toObject(Vaccination.class));
                            }
                            repository.getDueVaccines(bId, category, new VaccinationRepository.DataCallback() {
                                @Override
                                public void onDataLoaded(List<Vaccination> dueVaccines) {
                                    if (dueVaccines != null) totalDynamicDue.addAndGet(dueVaccines.size());
                                    db.collection("campaign_reminders").whereEqualTo("targetCategory", category).get()
                                        .addOnSuccessListener(campaigns -> {
                                            totalDynamicDue.addAndGet(campaigns.size());
                                            if (processed.incrementAndGet() == totalMembers) {
                                                updateSummary(allHistory, totalDynamicDue.get());
                                            }
                                        }).addOnFailureListener(err -> {
                                            if (processed.incrementAndGet() == totalMembers) updateSummary(allHistory, totalDynamicDue.get());
                                        });
                                }
                                @Override public void onError(String msg) {
                                    if (processed.incrementAndGet() == totalMembers) updateSummary(allHistory, totalDynamicDue.get());
                                }
                            });
                        });
                }
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) userListener.remove();
        if (memberListener != null) memberListener.remove();
        if (summaryListener != null) summaryListener.remove();
        if (reminderListener != null) reminderListener.remove();
    }

    private void updateSummary(List<Vaccination> vaccinationList, int reminderCount) {
        int completed = 0;
        int pending = reminderCount;
        
        for (Vaccination v : vaccinationList) {
            String status = v.getStatus() != null ? v.getStatus().toLowerCase() : "pending";
            if (status.contains("completed") || status.contains("done")) {
                completed++;
            } else {
                pending++;
            }
        }

        tvCompletedCount.setText(String.format("%02d", completed));
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

