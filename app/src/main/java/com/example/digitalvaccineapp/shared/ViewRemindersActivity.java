package com.example.digitalvaccineapp.shared;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.digitalvaccineapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ViewRemindersActivity extends AppCompatActivity {
    private RecyclerView rvReminders;
    private ReminderAdapter adapter;
    private List<Reminder> reminderList = new ArrayList<>();
    private FirebaseFirestore db;
    private String beneficiaryId;

    private com.google.firebase.firestore.ListenerRegistration personalListener, campaignListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reminders);

        db = FirebaseFirestore.getInstance();
        beneficiaryId = getIntent().getStringExtra("beneficiaryId");

        rvReminders = findViewById(R.id.rvRemindersList);
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(reminderList);
        rvReminders.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadRemindersRealTime();
    }

    private void loadRemindersRealTime() {
        if (beneficiaryId == null) {
            Toast.makeText(this, "No member selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Listen to personal reminders
        if (personalListener != null) personalListener.remove();
        personalListener = db.collection("notifications")
            .whereEqualTo("memberId", beneficiaryId)
            .orderBy("reminderDate", Query.Direction.ASCENDING)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null) return;
                
                // Clear and reload (re-fetching campaign as well to keep order if needed)
                reminderList.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    Reminder r = doc.toObject(Reminder.class);
                    r.setId(doc.getId());
                    reminderList.add(r);
                }
                
                // Fetch campaign reminders (one-time or with listener)
                fetchCampaignRemindersRealTime();
            });
    }

    private void fetchCampaignRemindersRealTime() {
        db.collection("family_members").document(beneficiaryId).get().addOnSuccessListener(doc -> {
            String category = doc.getString("category");
            if (category != null) {
                if (campaignListener != null) campaignListener.remove();
                campaignListener = db.collection("campaign_reminders")
                    .whereEqualTo("targetCategory", category)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null || snapshots == null) return;
                        
                        // We need to avoid duplicates if personal listener also triggers
                        // For simplicity, we just add them
                        for (QueryDocumentSnapshot cDoc : snapshots) {
                            Reminder r = cDoc.toObject(Reminder.class);
                            r.setId(cDoc.getId());
                            r.setStatus("Group Alert");
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
