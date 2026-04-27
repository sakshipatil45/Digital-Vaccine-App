package com.example.digitalvaccineapp.admin;

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
import com.example.digitalvaccineapp.admin.Beneficiary;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminReportsActivity extends AppCompatActivity {

    private ProgressBar progressBarReports;
    private ScrollView svReportsContent;
    
    private TextView tvTotalBeneficiaries, tvTotalVaccines;
    private TextView tvCountChild, tvCountPregnant, tvCountAdult;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarAdminReports);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("System Analytics");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBarReports = findViewById(R.id.progressBarReports);
        svReportsContent = findViewById(R.id.svReportsContent);
        
        tvTotalBeneficiaries = findViewById(R.id.tvTotalBeneficiaries);
        tvTotalVaccines = findViewById(R.id.tvTotalVaccines);
        tvCountChild = findViewById(R.id.tvCountChild);
        tvCountPregnant = findViewById(R.id.tvCountPregnant);
        tvCountAdult = findViewById(R.id.tvCountAdult);

        fetchGlobalAnalytics();
    }

    private void fetchGlobalAnalytics() {
        if (mAuth.getCurrentUser() == null) return;
        
        db.collection("family_members")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int totalBeneficiaries = task.getResult().size();
                    int countChild = 0;
                    int countPregnant = 0;
                    int countAdult = 0;
                    


                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Beneficiary b = doc.toObject(Beneficiary.class);
                        
                        // Demographic count
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



                    // Step 2: Aggregate vaccinations globally from the flat collection
                    db.collection("vaccinations").get().addOnCompleteListener(vaxTask -> {
                        if (vaxTask.isSuccessful()) {
                            tvTotalVaccines.setText(String.valueOf(vaxTask.getResult().size()));
                        } else {
                            tvTotalVaccines.setText("0");
                        }
                        
                        progressBarReports.setVisibility(View.GONE);
                        svReportsContent.setVisibility(View.VISIBLE);
                        Snackbar.make(findViewById(android.R.id.content), "Global Sync Complete", Snackbar.LENGTH_SHORT).show();
                    });
                } else {
                    progressBarReports.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content), "Failed to load global data.", Snackbar.LENGTH_LONG).show();
                }
            });
    }
}
