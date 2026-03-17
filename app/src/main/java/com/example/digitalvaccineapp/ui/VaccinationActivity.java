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
import com.example.digitalvaccineapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.TextView;

public class VaccinationActivity extends AppCompatActivity {
    private TextView tvWelcomeName;
    private VaccinationRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccination);

        repository = new VaccinationRepository(this);
        tvWelcomeName = findViewById(R.id.tvWelcomeName);

        // Fetch user name and dashboard counts
        loadDashboardData();

        // 1. Add Vaccine Button
        findViewById(R.id.btnDashAddVaccine).setOnClickListener(v -> {
            startActivity(new Intent(this, AddVaccinationActivity.class));
        });

        // 2. View Records Button
        findViewById(R.id.btnDashViewRecords).setOnClickListener(v -> {
            startActivity(new Intent(this, RecordsActivity.class));
        });

        // 3. Reminders Button (Placeholder - no explicit screen requested yet, could link to calendar or records)
        findViewById(R.id.btnDashReminders).setOnClickListener(v -> {
            Toast.makeText(this, "Reminders module opening...", Toast.LENGTH_SHORT).show();
            // Optional: Start a RemindersActivity if it exists
        });

        // 4. Profile Button
        findViewById(R.id.btnDashProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Also wire up the top right icon to profile as well
        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // 5. Logout Button
        findViewById(R.id.btnDashLogout).setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void loadDashboardData() {
        // Fetch Profile Name
        RetrofitClient.getApiService().getProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    String name = response.body().getData().getName();
                    if (name != null && !name.isEmpty()) {
                        tvWelcomeName.setText("Hello, " + name);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                tvWelcomeName.setText("Hello, User");
            }
        });

        // Fetch Vaccine counts
        repository.getVaccinations(new VaccinationRepository.DataCallback() {
            @Override
            public void onDataLoaded(List<Vaccination> vaccinations) {
                runOnUiThread(() -> {
                    updateSummary(vaccinations);
                });
            }

            @Override
            public void onError(String message) {
                // Handle error
            }
        });
    }

    private void updateSummary(List<Vaccination> vaccinationList) {
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
        
        tvCompleted.setText(String.valueOf(completed));
        tvPending.setText(String.valueOf(pending));
    }

    private void logoutUser() {
        // Clear SharedPreferences
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().clear().apply();
        
        // Sign out of Firebase
        FirebaseAuth.getInstance().signOut();
        
        // Redirect to Login/Welcome
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData(); // Refresh counts from local Room DB
    }
}
