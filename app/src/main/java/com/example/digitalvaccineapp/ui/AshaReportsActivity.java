package com.example.digitalvaccineapp.ui;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.models.Beneficiary;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AshaReportsActivity extends AppCompatActivity {

    private ProgressBar progressBarReports;
    private ScrollView svReportsContent;
    
    private TextView tvTotalBeneficiaries, tvTotalVaccines;
    private TextView tvCountChild, tvCountPregnant, tvCountAdult;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asha_reports);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarAshaReports);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBarReports = findViewById(R.id.progressBarReports);
        svReportsContent = findViewById(R.id.svReportsContent);
        
        tvTotalBeneficiaries = findViewById(R.id.tvTotalBeneficiaries);
        tvTotalVaccines = findViewById(R.id.tvTotalVaccines);
        tvCountChild = findViewById(R.id.tvCountChild);
        tvCountPregnant = findViewById(R.id.tvCountPregnant);
        tvCountAdult = findViewById(R.id.tvCountAdult);

        fetchAnalytics();
    }

    private void fetchAnalytics() {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();
        
        db.collection("users").document(userId).collection("beneficiaries")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int totalBeneficiaries = task.getResult().size();
                    int countChild = 0;
                    int countPregnant = 0;
                    int countAdult = 0;
                    
                    List<String> beneficiaryIds = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        beneficiaryIds.add(doc.getId());
                        Beneficiary b = doc.toObject(Beneficiary.class);
                        if (b.getCategory() != null) {
                            if (b.getCategory().equalsIgnoreCase("Child")) countChild++;
                            else if (b.getCategory().equalsIgnoreCase("Pregnant Woman")) countPregnant++;
                            else countAdult++;
                        }
                    }

                    tvTotalBeneficiaries.setText(String.valueOf(totalBeneficiaries));
                    tvCountChild.setText(String.valueOf(countChild));
                    tvCountPregnant.setText(String.valueOf(countPregnant));
                    tvCountAdult.setText(String.valueOf(countAdult));

                    if (totalBeneficiaries == 0) {
                        progressBarReports.setVisibility(View.GONE);
                        svReportsContent.setVisibility(View.VISIBLE);
                        return;
                    }

                    // Multi-query aggregation for vaccinations across all beneficiaries
                    AtomicInteger completedQueries = new AtomicInteger(0);
                    AtomicInteger totalVaccines = new AtomicInteger(0);

                    for (String bId : beneficiaryIds) {
                        db.collection("users").document(userId)
                            .collection("beneficiaries").document(bId)
                            .collection("vaccinations")
                            .get()
                            .addOnCompleteListener(vaxTask -> {
                                if (vaxTask.isSuccessful()) {
                                    totalVaccines.addAndGet(vaxTask.getResult().size());
                                }
                                
                                int current = completedQueries.incrementAndGet();
                                // Once all sub-queries finish
                                if (current == totalBeneficiaries) {
                                    tvTotalVaccines.setText(String.valueOf(totalVaccines.get()));
                                    progressBarReports.setVisibility(View.GONE);
                                    svReportsContent.setVisibility(View.VISIBLE);
                                    svReportsContent.scheduleLayoutAnimation();
                                }
                            });
                    }
                } else {
                    progressBarReports.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content), "Network error. Failed to map insights.", Snackbar.LENGTH_LONG).show();
                }
            });
    }
}
