package com.example.digitalvaccineapp.asha;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.shared.VaccinationAdapter;
import com.example.digitalvaccineapp.shared.Vaccination;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AshaAlertsActivity extends AppCompatActivity {

    private RecyclerView rvAlerts;
    private VaccinationAdapter adapter;
    private List<Vaccination> alertList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asha_alerts);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarAlerts);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvAlerts = findViewById(R.id.rvAlerts);
        rvAlerts.setLayoutManager(new LinearLayoutManager(this));
        alertList = new ArrayList<>();
        
        adapter = new VaccinationAdapter(alertList, new VaccinationAdapter.OnVaccinationClickListener() {
            @Override
            public void onEditClick(Vaccination vaccination) {
                // Read Only for Dashboard
                Toast.makeText(AshaAlertsActivity.this, "ASHA read-only view", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Vaccination vaccination) {
                // Read Only
                Toast.makeText(AshaAlertsActivity.this, "ASHA cannot delete citizen records", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(Vaccination vaccination) {
                Toast.makeText(AshaAlertsActivity.this, "Follow up with " + vaccination.getDependentName(), Toast.LENGTH_LONG).show();
            }
        });
        
        rvAlerts.setAdapter(adapter);

        loadAlerts();
    }

    private void loadAlerts() {
        // Fetch vaccinations globally to find dates. 
        // In a deployed app with full village structures, this filters by village map array.
        db.collection("vaccinations")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    alertList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Vaccination v = document.toObject(Vaccination.class);
                        v.setId(document.getId());
                        
                        // Fake logic: Just pull everything in for demonstration of the dashboard wire up.
                        alertList.add(v);
                    }
                    adapter.notifyDataSetChanged();
                    
                    if (alertList.isEmpty()) {
                        Toast.makeText(this, "No priority alerts found in your region", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to load alerts", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
