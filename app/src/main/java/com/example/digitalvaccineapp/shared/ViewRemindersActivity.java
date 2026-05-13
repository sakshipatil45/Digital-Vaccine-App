package com.example.digitalvaccineapp.shared;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.digitalvaccineapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewRemindersActivity extends AppCompatActivity {
    private RecyclerView rvReminders;
    private ReminderAdapter adapter;
    private List<Reminder> reminderList = new ArrayList<>();
    private FirebaseFirestore db;
    private String beneficiaryId;
    private String userPhone;

    private com.google.firebase.firestore.ListenerRegistration personalListener, campaignListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reminders);

        db = FirebaseFirestore.getInstance();
        beneficiaryId = getIntent().getStringExtra("beneficiaryId");
        userPhone = getIntent().getStringExtra("userPhone");

        rvReminders = findViewById(R.id.rvRemindersList);
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(reminderList);
        rvReminders.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadRemindersRealTime();
    }

    private void loadRemindersRealTime() {
        if (beneficiaryId != null) {
            // Case 1: Specific member selected
            loadForSingleMember(beneficiaryId);
        } else if (userPhone != null) {
            // Case 2: All Family Members (Aggregate)
            loadForAllFamily();
        } else {
            Toast.makeText(this, "No member context found", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadForSingleMember(String bId) {
        if (personalListener != null) personalListener.remove();
        
        // Fetch the member name first
        db.collection("family_members").document(bId).get().addOnSuccessListener(mDoc -> {
            String name = mDoc.getString("name");
            
            personalListener = db.collection("notifications")
                .whereEqualTo("memberId", bId)
                .orderBy("reminderDate", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    reminderList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Reminder r = doc.toObject(Reminder.class);
                        r.setId(doc.getId());
                        if (r.getMemberName() == null) r.setMemberName(name);
                        reminderList.add(r);
                    }
                    fetchCampaignRemindersRealTime(bId, name);
                });
        });
    }

    private void loadForAllFamily() {
        String cleanPhone = userPhone.replaceAll("[^0-9]", "");
        if (cleanPhone.length() > 10) cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
        List<String> variations = java.util.Arrays.asList(cleanPhone, "+91" + cleanPhone, "0" + cleanPhone);
        
        db.collection("family_members").whereIn("targetPhone", variations).get()
            .addOnSuccessListener(members -> {
                if (members.isEmpty()) {
                    Toast.makeText(this, "No family members found", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                List<String> memberIds = new ArrayList<>();
                List<String> categories = new ArrayList<>();
                Map<String, String> memberNameMap = new HashMap<>();
                
                for (QueryDocumentSnapshot mDoc : members) {
                    String mid = mDoc.getId();
                    memberIds.add(mid);
                    memberNameMap.put(mid, mDoc.getString("name"));
                    String cat = mDoc.getString("category");
                    if (cat != null && !categories.contains(cat)) categories.add(cat);
                }

                // Listen to notifications for ANY of these members
                if (personalListener != null) personalListener.remove();
                personalListener = db.collection("notifications")
                    .whereIn("memberId", memberIds)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null || snapshots == null) return;
                        reminderList.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Reminder r = doc.toObject(Reminder.class);
                            r.setId(doc.getId());
                            // Fill in name from map if missing in record
                            if (r.getMemberName() == null) r.setMemberName(memberNameMap.get(r.getMemberId()));
                            reminderList.add(r);
                        }
                        
                        // Also add campaign reminders for all relevant categories
                        if (!categories.isEmpty()) {
                            db.collection("campaign_reminders").whereIn("targetCategory", categories).get()
                                .addOnSuccessListener(campaigns -> {
                                    for (QueryDocumentSnapshot cDoc : campaigns) {
                                        Reminder r = cDoc.toObject(Reminder.class);
                                        r.setId(cDoc.getId());
                                        r.setStatus("Group Alert");
                                        r.setMemberName("Eligible Category Members");
                                        reminderList.add(r);
                                    }
                                    adapter.notifyDataSetChanged();
                                });
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    });
            });
    }

    private void fetchCampaignRemindersRealTime(String bId, String name) {
        db.collection("family_members").document(bId).get().addOnSuccessListener(doc -> {
            String category = doc.getString("category");
            if (category != null) {
                if (campaignListener != null) campaignListener.remove();
                campaignListener = db.collection("campaign_reminders")
                    .whereEqualTo("targetCategory", category)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null || snapshots == null) return;
                        for (QueryDocumentSnapshot cDoc : snapshots) {
                            Reminder r = cDoc.toObject(Reminder.class);
                            r.setId(cDoc.getId());
                            r.setStatus("Group Alert");
                            if (r.getMemberName() == null) r.setMemberName(name);
                            reminderList.add(r);
                        }
                        adapter.notifyDataSetChanged();
                    });
            } else {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (personalListener != null) personalListener.remove();
        if (campaignListener != null) campaignListener.remove();
    }
}
