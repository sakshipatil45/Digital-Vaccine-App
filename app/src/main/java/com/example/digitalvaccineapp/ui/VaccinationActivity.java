package com.example.digitalvaccineapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.adapter.VaccinationAdapter;
import com.example.digitalvaccineapp.models.ApiResponse;
import com.example.digitalvaccineapp.models.Vaccination;
import com.example.digitalvaccineapp.network.RetrofitClient;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VaccinationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private VaccinationAdapter adapter;
    private List<Vaccination> vaccinationList = new ArrayList<>();
    private VaccinationRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccination);

        repository = new VaccinationRepository(this);
        recyclerView = findViewById(R.id.rvVaccinations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VaccinationAdapter(vaccinationList);
        recyclerView.setAdapter(adapter);

        ExtendedFloatingActionButton btnAdd = findViewById(R.id.btnAddVaccination);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddVaccinationActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

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
                    updateSummary();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(VaccinationActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateSummary() {
        int completed = 0;
        int pending = 0;
        for (Vaccination v : vaccinationList) {
            String status = v.getStatus().toLowerCase();
            if (status.contains("completed") || status.contains("done")) {
                completed++;
            } else {
                pending++;
            }
        }
        
        TextView tvCompleted = findViewById(R.id.tvCompletedCount);
        TextView tvPending = findViewById(R.id.tvPendingCount);
        
        int finalCompleted = completed;
        int finalPending = pending;
        runOnUiThread(() -> {
            tvCompleted.setText(String.valueOf(finalCompleted));
            tvPending.setText(String.valueOf(finalPending));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchVaccinations(); // Refresh list when returning from Add activity
    }
}
