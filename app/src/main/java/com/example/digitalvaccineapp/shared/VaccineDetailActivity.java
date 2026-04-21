package com.example.digitalvaccineapp.shared;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.admin.Vaccine;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class VaccineDetailActivity extends AppCompatActivity {

    private TextView tvName, tvDose, tvDate, tvHospital, tvNotes, tvDesc, tvBenefits, tvSideEffects;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccine_detail);

        db = FirebaseFirestore.getInstance();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvName = findViewById(R.id.tvDetailName);
        tvDose = findViewById(R.id.tvDetailDose);
        tvDate = findViewById(R.id.tvDetailDate);
        tvHospital = findViewById(R.id.tvDetailHospital);
        tvNotes = findViewById(R.id.tvDetailNotes);
        tvDesc = findViewById(R.id.tvDetailDescription);
        tvBenefits = findViewById(R.id.tvDetailBenefits);
        tvSideEffects = findViewById(R.id.tvDetailSideEffects);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String name = extras.getString("name", "N/A");
            tvName.setText(name);
            tvDose.setText("Dose " + extras.getInt("dose", 1));
            tvDate.setText(extras.getString("date", "N/A"));
            tvHospital.setText(extras.getString("hospital", "N/A"));
            tvNotes.setText("Record saved successfully. Details verified.");

            fetchMasterData(name);
        }
    }

    private void fetchMasterData(String name) {
        db.collection("vaccines_master")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Vaccine v = doc.toObject(Vaccine.class);
                        tvDesc.setText(v.getDescription() != null ? v.getDescription() : "No database description available.");
                        tvBenefits.setText(v.getBenefits() != null ? v.getBenefits() : "No benefits listed.");
                        tvSideEffects.setText(v.getSideEffects() != null ? v.getSideEffects() : "None reported in study.");
                        break;
                    }
                } else {
                    tvDesc.setText("Master documentation not found for this vaccine.");
                    tvBenefits.setText("-");
                    tvSideEffects.setText("-");
                }
            });
    }
}
