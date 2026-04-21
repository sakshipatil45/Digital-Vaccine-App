package com.example.digitalvaccineapp.asha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.appcompat.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.asha.BeneficiaryAdapter;
import com.example.digitalvaccineapp.asha.Beneficiary;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BeneficiaryListActivity extends AppCompatActivity {

    private RecyclerView rvBeneficiaries;
    private LinearLayout llEmptyState;
    private ProgressBar progressBar;
    private ExtendedFloatingActionButton fabAddBeneficiary;
    private SearchView svBeneficiaries;
    
    private BeneficiaryAdapter adapter;
    private List<Beneficiary> beneficiaryList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beneficiary_list);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarBeneficiary);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvBeneficiaries = findViewById(R.id.rvBeneficiaries);
        llEmptyState = findViewById(R.id.llEmptyState);
        progressBar = findViewById(R.id.progressBar);
        fabAddBeneficiary = findViewById(R.id.fabAddBeneficiary);

        rvBeneficiaries.setLayoutManager(new LinearLayoutManager(this));
        beneficiaryList = new ArrayList<>();
        
        adapter = new BeneficiaryAdapter(beneficiaryList, beneficiary -> {
            Intent intent = new Intent(BeneficiaryListActivity.this, BeneficiaryDetailActivity.class);
            intent.putExtra("beneficiaryId", beneficiary.getId());
            intent.putExtra("beneficiaryName", beneficiary.getName());
            intent.putExtra("beneficiaryVillage", beneficiary.getVillage());
            intent.putExtra("beneficiaryAge", beneficiary.getAge());
            startActivity(intent);
        });
        
        rvBeneficiaries.setAdapter(adapter);

        svBeneficiaries = findViewById(R.id.svBeneficiaries);
        svBeneficiaries.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        fabAddBeneficiary.setOnClickListener(v -> {
            startActivity(new Intent(BeneficiaryListActivity.this, AddBeneficiaryActivity.class));
        });

        loadBeneficiaries();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBeneficiaries();
    }


    private void loadBeneficiaries() {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();
        
        // First fetch ASHA's village to filter correctly
        db.collection("users").document(userId).get()
            .addOnSuccessListener(userDoc -> {
                String ashaVillage = userDoc.getString("village");
                if (ashaVillage == null) {
                    progressBar.setVisibility(View.GONE);
                    llEmptyState.setVisibility(View.VISIBLE);
                    return;
                }

                // Path changed to filter by village for cross-role sync (ASHA + Citizen records)
                db.collection("beneficiaries")
                    .whereEqualTo("village", ashaVillage)
                    .get()
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            beneficiaryList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Beneficiary beneficiary = document.toObject(Beneficiary.class);
                                beneficiary.setId(document.getId());
                                beneficiaryList.add(beneficiary);
                            }
                            adapter.updateData(beneficiaryList);

                            if (beneficiaryList.isEmpty()) {
                                rvBeneficiaries.setVisibility(View.GONE);
                                llEmptyState.setVisibility(View.VISIBLE);
                            } else {
                                rvBeneficiaries.setVisibility(View.VISIBLE);
                                llEmptyState.setVisibility(View.GONE);
                            }
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "Failed to load beneficiaries", Snackbar.LENGTH_SHORT).show();
                        }
                    });
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(findViewById(android.R.id.content), "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
            });
    }
}
