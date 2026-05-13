package com.example.digitalvaccineapp.shared;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;

public class VaccineDetailActivity extends AppCompatActivity {

    private TextView tvName, tvDose, tvDate, tvHospital, tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccine_detail);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvName = findViewById(R.id.tvDetailName);
        tvDose = findViewById(R.id.tvDetailDose);
        tvDate = findViewById(R.id.tvDetailDate);
        tvHospital = findViewById(R.id.tvDetailHospital);
        tvStatus = findViewById(R.id.tvDetailStatus);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String name = extras.getString("name", "N/A");
            String status = extras.getString("status", "Pending");
            
            tvName.setText(name);
            tvDose.setText("Dose " + extras.getInt("dose", 1));
            tvDate.setText(extras.getString("date", "N/A"));
            tvHospital.setText(extras.getString("hospital", "N/A"));
            
            tvStatus.setText(status.toUpperCase());
            
            // Set Status Badge Color
            int colorRes = R.color.primary_purple;
            if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Done")) {
                colorRes = R.color.primary_teal;
            } else if (status.equalsIgnoreCase("Pending")) {
                colorRes = R.color.pending_orange;
            }
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(colorRes)));
        }
    }
}
