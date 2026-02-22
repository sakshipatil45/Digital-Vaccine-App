package com.example.digitalvaccineapp.ui;

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
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VaccinationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private VaccinationAdapter adapter;
    private List<Vaccination> vaccinationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccination);

        recyclerView = findViewById(R.id.rvVaccinations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VaccinationAdapter(vaccinationList);
        recyclerView.setAdapter(adapter);

        fetchVaccinations();
    }

    private void fetchVaccinations() {
        RetrofitClient.getApiService().getVaccinations().enqueue(new Callback<ApiResponse<List<Vaccination>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Vaccination>>> call, Response<ApiResponse<List<Vaccination>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    vaccinationList.clear();
                    vaccinationList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(VaccinationActivity.this, "Failed to load vaccinations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Vaccination>>> call, Throwable t) {
                Toast.makeText(VaccinationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
