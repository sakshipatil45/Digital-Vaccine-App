package com.example.digitalvaccineapp.shared;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.shared.VaccinationAdapter;
import com.example.digitalvaccineapp.shared.Vaccination;
import com.example.digitalvaccineapp.network.VaccinationRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RecordsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private VaccinationAdapter adapter;
    private List<Vaccination> vaccinationList = new ArrayList<>();
    private List<Vaccination> fullList = new ArrayList<>();
    private VaccinationRepository repository;
    private com.google.android.material.textfield.TextInputEditText etSearch;
    private android.widget.ProgressBar progressBar;
    private TextView tvCompletedCount, tvPendingCount;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String filterDependent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        repository = new VaccinationRepository(this);
        
        filterDependent = getIntent().getStringExtra("filterDependent");
        if (filterDependent != null) {
            com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle("Records: " + filterDependent);
        }

        recyclerView = findViewById(R.id.rvVaccinationsList);
        progressBar = findViewById(R.id.progressBar);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new VaccinationAdapter(vaccinationList, new VaccinationAdapter.OnVaccinationClickListener() {
            @Override
            public void onEditClick(Vaccination vaccination) {
                Intent intent = new Intent(RecordsActivity.this, AddVaccinationActivity.class);
                intent.putExtra("edit_mode", true);
                intent.putExtra("vax_id", vaccination.getId());
                intent.putExtra("beneficiary_id", vaccination.getPatientId());
                intent.putExtra("vax_name", vaccination.getVaccineName());
                intent.putExtra("vax_dose", vaccination.getDoseNumber());
                intent.putExtra("vax_date", vaccination.getDateTaken());
                intent.putExtra("vax_hospital", vaccination.getHospitalName());
                intent.putExtra("vax_dependent", vaccination.getDependentName());
                startActivity(intent);
            }

            @Override
            public void onReminderClick(Vaccination vaccination) {
                Intent intent = new Intent(RecordsActivity.this, ReminderActivity.class);
                intent.putExtra("force_vaccine", vaccination.getVaccineName());
                intent.putExtra("force_patient", vaccination.getDependentName());
                intent.putExtra("force_member_id", vaccination.getPatientId());
                intent.putExtra("force_hospital", vaccination.getHospitalName());
                intent.putExtra("force_date", vaccination.getDateTaken());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Vaccination vaccination) {
                deleteRecord(vaccination);
            }

            @Override
            public void onItemClick(Vaccination vaccination) {
                Intent intent = new Intent(RecordsActivity.this, VaccineDetailActivity.class);
                intent.putExtra("name", vaccination.getVaccineName());
                intent.putExtra("dose", vaccination.getDoseNumber());
                intent.putExtra("date", vaccination.getDateTaken());
                intent.putExtra("hospital", vaccination.getHospitalName());
                intent.putExtra("status", vaccination.getStatus());
                startActivity(intent);
            }
        }, true);
        
        recyclerView.setAdapter(adapter);

        etSearch = findViewById(R.id.etSearchRecords);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { filterRecords(s.toString()); }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        fetchVaccinations();
    }
    
    private void deleteRecord(Vaccination vaccination) {
        if (vaccination.getId() == null || vaccination.getPatientId() == null) {
            Toast.makeText(this, "Cannot delete record: Missing IDs", Toast.LENGTH_SHORT).show();
            return;
        }
        
        repository.deleteVaccination(vaccination.getPatientId(), vaccination.getId(), new VaccinationRepository.DataCallback() {
            @Override
            public void onDataLoaded(List<Vaccination> vaccinations) {
                Toast.makeText(RecordsActivity.this, "Record deleted from cloud", Toast.LENGTH_SHORT).show();
                fetchVaccinations(); 
            }
            @Override
            public void onError(String message) {
                Toast.makeText(RecordsActivity.this, "Delete failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterRecords(String query) {
        List<Vaccination> filtered = new ArrayList<>();
        for (Vaccination v : fullList) {
            boolean matchesQuery = v.getVaccineName().toLowerCase().contains(query.toLowerCase()) ||
                v.getHospitalName().toLowerCase().contains(query.toLowerCase()) ||
                v.getDependentName().toLowerCase().contains(query.toLowerCase());
            
            boolean matchesMember = (filterDependent == null) || (v.getDependentName() != null && v.getDependentName().equalsIgnoreCase(filterDependent));
            
            if (matchesQuery && matchesMember) {
                filtered.add(v);
            }
        }
        vaccinationList.clear();
        vaccinationList.addAll(filtered);
        adapter.notifyDataSetChanged();
        updateCounts(filtered);
    }

    private void fetchVaccinations() {
        if (mAuth.getCurrentUser() == null) return;
        progressBar.setVisibility(View.VISIBLE);

        String uid = mAuth.getCurrentUser().getUid();
        fullList.clear();
        
        // Fetch by userId (for now, will expand to phone if needed)
        db.collection("vaccinations").whereEqualTo("userId", uid).get()
            .addOnSuccessListener(vaxDocs -> {
                for (QueryDocumentSnapshot vaxDoc : vaxDocs) {
                    Vaccination v = vaxDoc.toObject(Vaccination.class);
                    v.setId(vaxDoc.getId());
                    v.setPatientId(vaxDoc.getString("memberId"));
                    fullList.add(v);
                }
                finishLoading();
            })
            .addOnFailureListener(e -> {
                finishLoading();
                Toast.makeText(this, "Failed to load records", Toast.LENGTH_SHORT).show();
            });
    }

    private void finishLoading() {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            vaccinationList.clear();
            
            List<Vaccination> filtered = new ArrayList<>();
            for (Vaccination v : fullList) {
                if (filterDependent == null || (v.getDependentName() != null && v.getDependentName().equalsIgnoreCase(filterDependent))) {
                    filtered.add(v);
                }
            }
            
            vaccinationList.addAll(filtered);
            adapter.notifyDataSetChanged();
            updateCounts(filtered);
        });
    }

    private void updateCounts(List<Vaccination> list) {
        int completed = 0;
        int pending = 0;
        for (Vaccination v : list) {
            String status = v.getStatus() != null ? v.getStatus().toLowerCase() : "pending";
            if (status.contains("completed") || status.contains("done")) {
                completed++;
            } else {
                pending++;
            }
        }
        tvCompletedCount.setText(String.valueOf(completed));
        tvPendingCount.setText(String.valueOf(pending));
    }
}
