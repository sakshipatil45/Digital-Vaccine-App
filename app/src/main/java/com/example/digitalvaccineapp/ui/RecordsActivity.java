package com.example.digitalvaccineapp.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.adapter.VaccinationAdapter;
import com.example.digitalvaccineapp.models.Vaccination;
import com.example.digitalvaccineapp.network.VaccinationRepository;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class RecordsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private VaccinationAdapter adapter;
    private List<Vaccination> vaccinationList = new ArrayList<>();
    private VaccinationRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        repository = new VaccinationRepository(this);
        recyclerView = findViewById(R.id.rvVaccinationsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new VaccinationAdapter(vaccinationList, new VaccinationAdapter.OnVaccinationClickListener() {
            @Override
            public void onEditClick(Vaccination vaccination) {
                // Feature extension: Open AddVaccinationActivity with extras for editing
                Intent intent = new Intent(RecordsActivity.this, AddVaccinationActivity.class);
                // In a full implementation, you'd pass IDs to Edit, then use PUT API
                Toast.makeText(RecordsActivity.this, "Edit Mode: " + vaccination.getVaccineName(), Toast.LENGTH_SHORT).show();
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

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        fetchVaccinations();
    }
    
    private void deleteRecord(Vaccination vaccination) {
        if (vaccination.getId() == null) {
            Toast.makeText(this, "Cannot delete un-synced record.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(this, "Deleting " + vaccination.getVaccineName() + "...", Toast.LENGTH_SHORT).show();
        
        com.example.digitalvaccineapp.network.RetrofitClient.getApiService().deleteVaccination(vaccination.getId())
            .enqueue(new retrofit2.Callback<com.example.digitalvaccineapp.models.ApiResponse<Void>>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.digitalvaccineapp.models.ApiResponse<Void>> call, retrofit2.Response<com.example.digitalvaccineapp.models.ApiResponse<Void>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(RecordsActivity.this, "Record deleted", Toast.LENGTH_SHORT).show();
                        fetchVaccinations(); // Refresh list
                    } else {
                        Toast.makeText(RecordsActivity.this, "Failed to delete from server", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.digitalvaccineapp.models.ApiResponse<Void>> call, Throwable t) {
                    Toast.makeText(RecordsActivity.this, "Error deleting: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void fetchVaccinations() {
        repository.getVaccinations(new VaccinationRepository.DataCallback() {
            @Override
            public void onDataLoaded(List<Vaccination> vaccinations) {
                runOnUiThread(() -> {
                    vaccinationList.clear();
                    vaccinationList.addAll(vaccinations);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(RecordsActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
