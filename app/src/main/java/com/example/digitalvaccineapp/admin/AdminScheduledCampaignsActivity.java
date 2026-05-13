package com.example.digitalvaccineapp.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminScheduledCampaignsActivity extends AppCompatActivity {

    private RecyclerView rvCampaigns;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;

    private CampaignAdapter adapter;
    private List<Campaign> campaignList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_scheduled_campaigns);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarCampaigns);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Scheduled Campaigns");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvCampaigns = findViewById(R.id.rvCampaigns);
        llEmptyState = findViewById(R.id.llEmptyState);
        progressBar = findViewById(R.id.progressBar);

        rvCampaigns.setLayoutManager(new LinearLayoutManager(this));
        campaignList = new ArrayList<>();
        adapter = new CampaignAdapter(campaignList, this::showReminderConfirmation);
        rvCampaigns.setAdapter(adapter);

        loadCampaigns();
    }

    private void showReminderConfirmation(Campaign campaign) {
        android.content.Intent intent = new android.content.Intent(this, AdminAnnouncementsActivity.class);
        intent.putExtra("prefill_title", "Reminder: " + campaign.getVaccineName() + " Drive");
        intent.putExtra("prefill_message", "Friendly reminder for the upcoming " + campaign.getVaccineName() + 
                       " vaccination drive scheduled for " + campaign.getTargetCategory() + 
                       ". Please ensure your presence at the venue.");
        intent.putExtra("prefill_date", campaign.getReminderDate());
        intent.putExtra("prefill_time", campaign.getTime());
        intent.putExtra("prefill_location", campaign.getPlace());
        startActivity(intent);
    }

    private void loadCampaigns() {
        if (mAuth.getCurrentUser() == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        db.collection("campaign_reminders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    campaignList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Campaign campaign = document.toObject(Campaign.class);
                        campaign.setId(document.getId());
                        campaignList.add(campaign);
                    }
                    adapter.updateData(campaignList);

                    if (campaignList.isEmpty()) {
                        rvCampaigns.setVisibility(View.GONE);
                        llEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        rvCampaigns.setVisibility(View.VISIBLE);
                        llEmptyState.setVisibility(View.GONE);
                    }
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Failed to load campaigns", Snackbar.LENGTH_SHORT).show();
                }
            });
    }
}
