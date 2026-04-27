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
        
        // Admin fetches ALL vaccinations to check for alerts
        db.collection("vaccinations")
            .get()
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    alertList.clear();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Vaccination v = doc.toObject(Vaccination.class);
                        v.setId(doc.getId());
                        v.setPatientId(doc.getString("memberId"));
                        
                        // Filter for pending/alert logic
                        if (v.getStatus() != null && v.getStatus().equalsIgnoreCase("pending")) {
                            alertList.add(v);
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                    if (alertList.isEmpty()) {
                        Toast.makeText(this, "Everything is up to date! 🎉", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to load alerts.", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
