package com.example.digitalvaccineapp.asha;
import com.example.digitalvaccineapp.shared.AddVaccinationActivity;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.shared.VaccinationAdapter;
import com.example.digitalvaccineapp.shared.Vaccination;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BeneficiaryDetailActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileDetails;
    private RecyclerView rvVaccinations;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;
    private ImageButton btnAddRecord, btnEditProfile, btnDeleteProfile;

    private VaccinationAdapter adapter;
    private List<Vaccination> vaccinationList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String beneficiaryId, beneficiaryName, beneficiaryVillage, beneficiaryAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beneficiary_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        beneficiaryId = getIntent().getStringExtra("beneficiaryId");
        beneficiaryName = getIntent().getStringExtra("beneficiaryName");
        beneficiaryVillage = getIntent().getStringExtra("beneficiaryVillage");
        beneficiaryAge = getIntent().getStringExtra("beneficiaryAge");

        Toolbar toolbar = findViewById(R.id.toolbarBeneficiaryDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileDetails = findViewById(R.id.tvProfileDetails);
        rvVaccinations = findViewById(R.id.rvVaccinations);
        llEmptyState = findViewById(R.id.llEmptyState);
        progressBar = findViewById(R.id.progressBar);
        btnAddRecord = findViewById(R.id.btnAddRecord);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDeleteProfile = findViewById(R.id.btnDeleteProfile);

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(BeneficiaryDetailActivity.this, AddBeneficiaryActivity.class);
            intent.putExtra("edit_mode", true);
            intent.putExtra("beneficiaryId", beneficiaryId);
            startActivity(intent);
        });

        btnDeleteProfile.setOnClickListener(v -> deleteBeneficiary());

        tvProfileName.setText(beneficiaryName != null ? beneficiaryName : "Unknown Patient");
        tvProfileDetails.setText(beneficiaryVillage + " • " + beneficiaryAge + " yrs");

        rvVaccinations.setLayoutManager(new LinearLayoutManager(this));
        vaccinationList = new ArrayList<>();
        
        adapter = new VaccinationAdapter(vaccinationList, new VaccinationAdapter.OnVaccinationClickListener() {
            @Override
            public void onEditClick(Vaccination vaccination) {
                Snackbar.make(findViewById(android.R.id.content), "Edit locked in Beneficiary View", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Vaccination vaccination) {
                deleteVaccination(vaccination.getId());
            }

            @Override
            public void onItemClick(Vaccination vaccination) {
                // Ignore
            }
        }, false);
        rvVaccinations.setAdapter(adapter);

        btnAddRecord.setOnClickListener(v -> {
            Intent intent = new Intent(BeneficiaryDetailActivity.this, AddVaccinationActivity.class);
            intent.putExtra("force_dependent", beneficiaryName);
            intent.putExtra("asha_beneficiary_mode", true);
            intent.putExtra("beneficiary_id", beneficiaryId);
            startActivity(intent);
        });

        loadVaccinations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVaccinations();
    }

    private void loadVaccinations() {
        if (mAuth.getCurrentUser() == null || beneficiaryId == null) return;
        
        // Path updated for Global Registry Sync
        db.collection("beneficiaries").document(beneficiaryId)
            .collection("vaccinations")
            .get()
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    vaccinationList.clear();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Vaccination v = doc.toObject(Vaccination.class);
                        v.setId(doc.getId());
                        vaccinationList.add(v);
                    }
                    adapter.notifyDataSetChanged();
                    
                    if (vaccinationList.isEmpty()) {
                        rvVaccinations.setVisibility(View.GONE);
                        llEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        rvVaccinations.setVisibility(View.VISIBLE);
                        llEmptyState.setVisibility(View.GONE);
                    }
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Error pulling records", Snackbar.LENGTH_SHORT).show();
                }
            });
    }

    private void deleteBeneficiary() {
        if (beneficiaryId == null) return;
        
        // Path updated for Global Registry Sync
        db.collection("beneficiaries").document(beneficiaryId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Snackbar.make(findViewById(android.R.id.content), "Beneficiary record removed", Snackbar.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Snackbar.make(findViewById(android.R.id.content), "Delete failed: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
            });
    }

    private void deleteVaccination(String vaxId) {
        if (vaxId == null || beneficiaryId == null) return;
        
        // Path updated for Global Registry Sync
        db.collection("beneficiaries").document(beneficiaryId)
            .collection("vaccinations").document(vaxId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Snackbar.make(findViewById(android.R.id.content), "Record purged from shared registry", Snackbar.LENGTH_SHORT).show();
                loadVaccinations();
            })
            .addOnFailureListener(e -> {
                Snackbar.make(findViewById(android.R.id.content), "Delete failed: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
            });
    }
}
