package com.example.digitalvaccineapp.admin;
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

    private TextView tvProfileName, tvProfileDetails, tvExtraDetails, tvLinkedPhone;
    private RecyclerView rvVaccinations;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;
    private ImageButton btnAddRecord, btnEditProfile, btnDeleteProfile;

    private VaccinationAdapter adapter;
    private List<Vaccination> vaccinationList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String beneficiaryId, beneficiaryName, beneficiaryAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beneficiary_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        beneficiaryId = getIntent().getStringExtra("beneficiaryId");
        beneficiaryName = getIntent().getStringExtra("beneficiaryName");

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
        tvExtraDetails = findViewById(R.id.tvExtraDetails);
        tvLinkedPhone = findViewById(R.id.tvLinkedPhone);
        rvVaccinations = findViewById(R.id.rvVaccinations);
        llEmptyState = findViewById(R.id.llEmptyState);
        progressBar = findViewById(R.id.progressBar);
        btnAddRecord = findViewById(R.id.btnAddRecord);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDeleteProfile = findViewById(R.id.btnDeleteProfile);

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(BeneficiaryDetailActivity.this, com.example.digitalvaccineapp.citizen.AddFamilyMemberActivity.class);
            intent.putExtra("edit_mode", true);
            intent.putExtra("beneficiaryId", beneficiaryId);
            startActivity(intent);
        });

        btnDeleteProfile.setOnClickListener(v -> deleteBeneficiary());

        tvProfileName.setText(beneficiaryName != null ? beneficiaryName : "Unknown Patient");
        tvProfileDetails.setText(beneficiaryAge + " yrs");

        rvVaccinations.setLayoutManager(new LinearLayoutManager(this));
        vaccinationList = new ArrayList<>();
        
        adapter = new VaccinationAdapter(vaccinationList, new VaccinationAdapter.OnVaccinationClickListener() {
            @Override
            public void onEditClick(Vaccination vaccination) {
                Intent intent = new Intent(BeneficiaryDetailActivity.this, AddVaccinationActivity.class);
                intent.putExtra("edit_mode", true);
                intent.putExtra("vax_id", vaccination.getId());
                intent.putExtra("vax_name", vaccination.getVaccineName());
                intent.putExtra("vax_dose", vaccination.getDoseNumber());
                intent.putExtra("vax_date", vaccination.getDateTaken());
                intent.putExtra("vax_hospital", vaccination.getHospitalName());
                intent.putExtra("beneficiary_id", beneficiaryId);
                intent.putExtra("force_dependent", beneficiaryName);
                startActivity(intent);
            }

            @Override
            public void onReminderClick(Vaccination vaccination) {
                Intent intent = new Intent(BeneficiaryDetailActivity.this, com.example.digitalvaccineapp.shared.ReminderActivity.class);
                intent.putExtra("force_vaccine", vaccination.getVaccineName());
                intent.putExtra("force_patient", vaccination.getDependentName());
                startActivity(intent);
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
            intent.putExtra("admin_mode", true);
            intent.putExtra("beneficiary_id", beneficiaryId);
            startActivity(intent);
        });

        loadBeneficiaryData();
        loadVaccinations();
    }

    private void loadBeneficiaryData() {
        if (beneficiaryId == null) return;
        
        db.collection("family_members").document(beneficiaryId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String userId = documentSnapshot.getString("userId");
                    
                    if (userId != null) {
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener(userDoc -> {
                                if (userDoc.exists()) {
                                    String phone = userDoc.getString("phone");
                                    if (phone != null) {
                                        tvLinkedPhone.setText("📞 Mobile: " + phone);
                                    }
                                }
                            });
                    }

                    String category = documentSnapshot.getString("category");
                    String mother = documentSnapshot.getString("motherName");
                    String father = documentSnapshot.getString("fatherName");
                    String husband = documentSnapshot.getString("husbandName");

                    if (category != null && category.contains("year")) {
                        StringBuilder sb = new StringBuilder();
                        if (mother != null && !mother.isEmpty()) sb.append("Mother: ").append(mother);
                        if (father != null && !father.isEmpty()) {
                            if (sb.length() > 0) sb.append(" • ");
                            sb.append("Father: ").append(father);
                        }
                        tvExtraDetails.setText(sb.toString());
                        tvExtraDetails.setVisibility(View.VISIBLE);
                    } else if ("Pregnant Women".equals(category) && husband != null && !husband.isEmpty()) {
                        tvExtraDetails.setText("Husband: " + husband);
                        tvExtraDetails.setVisibility(View.VISIBLE);
                    }
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBeneficiaryData();
        loadVaccinations();
    }

    private void loadVaccinations() {
        if (mAuth.getCurrentUser() == null || beneficiaryId == null) return;
        
        // Path updated for Global Registry Sync
        db.collection("vaccinations")
            .whereEqualTo("memberId", beneficiaryId)
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
        db.collection("family_members").document(beneficiaryId)
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
        db.collection("vaccinations").document(vaxId)
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
