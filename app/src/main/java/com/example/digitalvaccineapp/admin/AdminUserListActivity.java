package com.example.digitalvaccineapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.appcompat.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.admin.BeneficiaryAdapter;
import com.example.digitalvaccineapp.admin.Beneficiary;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminUserListActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_admin_user_list);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarBeneficiary);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("System Registry");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvBeneficiaries = findViewById(R.id.rvBeneficiaries);
        llEmptyState = findViewById(R.id.llEmptyState);
        progressBar = findViewById(R.id.progressBar);
        fabAddBeneficiary = findViewById(R.id.fabAddBeneficiary);

        rvBeneficiaries.setLayoutManager(new LinearLayoutManager(this));
        beneficiaryList = new ArrayList<>();
        
        adapter = new BeneficiaryAdapter(beneficiaryList, beneficiary -> {
            Intent intent = new Intent(AdminUserListActivity.this, BeneficiaryDetailActivity.class);
            intent.putExtra("beneficiaryId", beneficiary.getId());
            intent.putExtra("beneficiaryName", beneficiary.getName());

            intent.putExtra("beneficiaryAge", beneficiary.getAge());
            startActivity(intent);
        });
        
        rvBeneficiaries.setAdapter(adapter);

        svBeneficiaries = findViewById(R.id.svBeneficiaries);
        svBeneficiaries.setQueryHint("Search by Name, Category or ID");
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
            startActivity(new Intent(AdminUserListActivity.this, AdminAddUserActivity.class));
        });

        loadGlobalUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGlobalUsers();
    }

    private void loadGlobalUsers() {
        if (mAuth.getCurrentUser() == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        // Admin fetches ALL family members globally
        db.collection("family_members")
            .get()
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    beneficiaryList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Beneficiary beneficiary = new Beneficiary();
                        beneficiary.setId(document.getId());
                        beneficiary.setName(document.getString("name"));

                        beneficiary.setCategory(document.getString("category"));
                        
                        // Handle age which might be stored as String or Number
                        Object ageObj = document.get("age");
                        if (ageObj != null) {
                            beneficiary.setAge(ageObj.toString());
                        }
                        
                        // Link phone from userId if needed
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
                    Snackbar.make(findViewById(android.R.id.content), "Failed to sync system registry", Snackbar.LENGTH_SHORT).show();
                }
            });
    }
}
