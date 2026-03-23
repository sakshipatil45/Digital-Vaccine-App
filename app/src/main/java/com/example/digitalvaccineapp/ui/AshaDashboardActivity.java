package com.example.digitalvaccineapp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalvaccineapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AshaDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeAsha, tvTotalBeneficiaries, tvOverdueAlerts;
    private ImageButton btnProfileAsha;
    private MaterialButton btnAddBeneficiary, btnViewRecords, btnSchedule, btnAlerts, btnReports, btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asha_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvWelcomeAsha = findViewById(R.id.tvWelcomeAsha);
        tvTotalBeneficiaries = findViewById(R.id.tvTotalBeneficiaries);
        tvOverdueAlerts = findViewById(R.id.tvOverdueAlerts);
        btnProfileAsha = findViewById(R.id.btnProfileAsha);

        btnAddBeneficiary = findViewById(R.id.btnAshaAddBeneficiary);
        btnViewRecords = findViewById(R.id.btnAshaViewRecords);
        btnSchedule = findViewById(R.id.btnAshaSchedule);
        btnAlerts = findViewById(R.id.btnAshaAlerts);
        btnReports = findViewById(R.id.btnAshaReports);
        btnLogout = findViewById(R.id.btnAshaLogout);

        loadAshaProfile();

        btnProfileAsha.setOnClickListener(v -> {
            startActivity(new Intent(AshaDashboardActivity.this, ProfileActivity.class));
        });

        btnAddBeneficiary.setOnClickListener(v -> {
            Toast.makeText(this, "Add Beneficiary module coming soon", Toast.LENGTH_SHORT).show();
            // Link to Add Beneficiary screen in future iterations
        });

        btnViewRecords.setOnClickListener(v -> {
            startActivity(new Intent(AshaDashboardActivity.this, RecordsActivity.class));
        });

        btnSchedule.setOnClickListener(v -> {
            startActivity(new Intent(AshaDashboardActivity.this, VaccineInfoActivity.class)); // Can reuse Intelligence Center as stub
        });

        btnAlerts.setOnClickListener(v -> {
            startActivity(new Intent(AshaDashboardActivity.this, AshaAlertsActivity.class));
        });

        btnReports.setOnClickListener(v -> {
            Toast.makeText(this, "Reports & Analytics module coming soon", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadAshaProfile() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String name = document.getString("name");
                        if (name != null) {
                            tvWelcomeAsha.setText("Welcome, " + name + " 👩‍⚕️");
                        }
                    }
                });
        }
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.remove("userRole");
        editor.apply();

        mAuth.signOut();
        Toast.makeText(this, "Logged out safely", Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
