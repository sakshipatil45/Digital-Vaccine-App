package com.example.digitalvaccineapp.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.shared.VaccinationAdapter;
import com.example.digitalvaccineapp.shared.Vaccination;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminAlertsActivity extends AppCompatActivity {

    private RecyclerView rvAlerts;
    private ProgressBar progressBar;
    private VaccinationAdapter adapter;
    private List<Vaccination> alertList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_alerts);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarAlerts);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("System Due List");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvAlerts = findViewById(R.id.rvAlerts);
        progressBar = findViewById(R.id.progressBar);
        rvAlerts.setLayoutManager(new LinearLayoutManager(this));
        alertList = new ArrayList<>();
        
        adapter = new VaccinationAdapter(alertList, new VaccinationAdapter.OnVaccinationClickListener() {
            @Override public void onEditClick(Vaccination vaccination) { }
            @Override public void onDeleteClick(Vaccination vaccination) { }
            @Override public void onReminderClick(Vaccination vaccination) {
                android.content.Intent intent = new android.content.Intent(AdminAlertsActivity.this, com.example.digitalvaccineapp.shared.ReminderActivity.class);
                intent.putExtra("force_vaccine", vaccination.getVaccineName());
                intent.putExtra("force_patient", vaccination.getDependentName());
                startActivity(intent);
            }
            @Override public void onItemClick(Vaccination vaccination) {
                Toast.makeText(AdminAlertsActivity.this, "Priority follow-up needed for: " + vaccination.getDependentName(), Toast.LENGTH_SHORT).show();
            }
        }, false);
        
        rvAlerts.setAdapter(adapter);
        loadGlobalAlerts();
    }

    private void loadGlobalAlerts() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        // Admin fetches ALL beneficiaries to check for alerts
        db.collection("beneficiaries")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    List<String> ids = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : task.getResult()) ids.add(doc.getId());
                    
                    aggregateVaccinations(ids);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "No records found in system.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void aggregateVaccinations(List<String> patientIds) {
        AtomicInteger processed = new AtomicInteger(0);
        int total = patientIds.size();
        alertList.clear();

        for (String id : patientIds) {
            db.collection("beneficiaries").document(id).collection("vaccinations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Vaccination v = doc.toObject(Vaccination.class);
                            v.setId(doc.getId());
                            v.setPatientId(id);
                            // Filter for pending/alert logic
                            if (v.getStatus() != null && v.getStatus().equalsIgnoreCase("pending")) {
                                alertList.add(v);
                            }
                        }
                    }
                    
                    if (processed.incrementAndGet() == total) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                            if (alertList.isEmpty()) {
                                Toast.makeText(this, "Everything is up to date! 🎉", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
        }
    }
}
