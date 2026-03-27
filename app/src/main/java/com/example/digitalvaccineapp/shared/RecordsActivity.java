package com.example.digitalvaccineapp.shared;

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
import java.util.ArrayList;
import java.util.List;

public class RecordsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private VaccinationAdapter adapter;
    private List<Vaccination> vaccinationList = new ArrayList<>();
    private List<Vaccination> fullList = new ArrayList<>();
    private VaccinationRepository repository;
    private com.google.android.material.textfield.TextInputEditText etSearch;
    private android.widget.ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        repository = new VaccinationRepository(this);
        recyclerView = findViewById(R.id.rvVaccinationsList);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new VaccinationAdapter(vaccinationList, new VaccinationAdapter.OnVaccinationClickListener() {
            @Override
            public void onEditClick(Vaccination vaccination) {
                Intent intent = new Intent(RecordsActivity.this, AddVaccinationActivity.class);
                intent.putExtra("edit_mode", true);
                intent.putExtra("vax_id", vaccination.getId());
                intent.putExtra("vax_name", vaccination.getVaccineName());
                intent.putExtra("vax_dose", vaccination.getDoseNumber());
                intent.putExtra("vax_date", vaccination.getDateTaken());
                intent.putExtra("vax_hospital", vaccination.getHospitalName());
                intent.putExtra("vax_dependent", vaccination.getDependentName());
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
                startActivity(intent);
            }
        });
        
        recyclerView.setAdapter(adapter);

        etSearch = findViewById(R.id.etSearchRecords);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecords(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        fetchVaccinations();
    }
    
    private void deleteRecord(Vaccination vaccination) {
        if (vaccination.getId() == null) {
            com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Cannot delete un-synced record.", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
            return;
        }
        
        com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Deleting from Cloud...", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        
        repository.deleteVaccination(vaccination.getId(), new VaccinationRepository.DataCallback() {
            @Override
            public void onDataLoaded(List<Vaccination> vaccinations) {
                com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Record deleted successfully", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                fetchVaccinations(); // Refresh list
            }

            @Override
            public void onError(String message) {
                com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Error deleting: " + message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void filterRecords(String query) {
        List<Vaccination> filtered = new ArrayList<>();
        for (Vaccination v : fullList) {
            if (v.getVaccineName().toLowerCase().contains(query.toLowerCase()) ||
                v.getHospitalName().toLowerCase().contains(query.toLowerCase()) ||
                v.getDependentName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(v);
            }
        }
        vaccinationList.clear();
        vaccinationList.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    private void fetchVaccinations() {
        repository.getVaccinations(new VaccinationRepository.DataCallback() {
            @Override
            public void onDataLoaded(List<Vaccination> vaccinations) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    if (vaccinations != null) {
                        fullList.clear();
                        fullList.addAll(vaccinations);
                        
                        String filterDependent = getIntent().getStringExtra("filterDependent");
                        vaccinationList.clear();
                        if (filterDependent != null && !filterDependent.isEmpty()) {
                            for (Vaccination v : vaccinations) {
                                if (filterDependent.equalsIgnoreCase(v.getDependentName())) {
                                    vaccinationList.add(v);
                                }
                            }
                        } else {
                            vaccinationList.addAll(vaccinations);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                });
            }
        });
    }
}
