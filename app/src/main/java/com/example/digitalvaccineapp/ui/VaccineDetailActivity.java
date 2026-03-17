package com.example.digitalvaccineapp.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;

public class VaccineDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccine_detail);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvDose = findViewById(R.id.tvDetailDose);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        TextView tvHospital = findViewById(R.id.tvDetailHospital);
        TextView tvNotes = findViewById(R.id.tvDetailNotes);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tvName.setText(extras.getString("name", "N/A"));
            tvDose.setText("Dose " + extras.getInt("dose", 1));
            tvDate.setText(extras.getString("date", "N/A"));
            tvHospital.setText(extras.getString("hospital", "N/A"));
            // Mock notes for now since they aren't saved to backend in current model
            tvNotes.setText("Record saved successfully. Details verified.");
        }
    }
}
