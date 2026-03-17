package com.example.digitalvaccineapp.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.adapter.VaccinationAdapter;
import com.example.digitalvaccineapp.models.Vaccination;

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
        adapter = new VaccinationAdapter(vaccinationList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        fetchVaccinations();
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
