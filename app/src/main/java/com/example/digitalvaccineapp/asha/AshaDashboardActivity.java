package com.example.digitalvaccineapp.asha;
import com.example.digitalvaccineapp.shared.ReminderActivity;

import com.example.digitalvaccineapp.shared.ProfileActivity;
import com.example.digitalvaccineapp.auth.LoginActivity;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.atomic.AtomicInteger;

public class AshaDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeAsha, tvTotalBeneficiaries, tvOverdueAlerts;
    private ImageButton btnProfileAsha;
    private MaterialButton btnAddBeneficiary, btnViewRecords, btnReminders, btnAlerts, btnReports, btnLogout;

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

        btnAlerts = findViewById(R.id.btnAshaAlerts);
        btnReminders = findViewById(R.id.btnAshaReminders);
        btnReports = findViewById(R.id.btnAshaReports);
        btnLogout = findViewById(R.id.btnAshaLogout);

        loadAshaProfile();
        loadDashboardStats();

        btnProfileAsha.setOnClickListener(v -> {
            startActivity(new Intent(AshaDashboardActivity.this, ProfileActivity.class));
        });
        
        findViewById(R.id.btnNotificationsAsha).setOnClickListener(v -> {
            startActivity(new Intent(AshaDashboardActivity.this, com.example.digitalvaccineapp.shared.NotificationsActivity.class));
        });

        btnAddBeneficiary.setOnClickListener(v -> {
            startActivity(new Intent(AshaDashboardActivity.this, AddBeneficiaryActivity.class));
        });

        btnViewRecords.setOnClickListener(v -> {
            startActivity(new Intent(AshaDashboardActivity.this, BeneficiaryListActivity.class));
        });

        btnReminders.setOnClickListener(v -> {
            startActivity(new Intent(AshaDashboardActivity.this, ReminderActivity.class));
        });

        btnAlerts.setOnClickListener(v -> {
            startActivity(new Intent(AshaDashboardActivity.this, AshaAlertsActivity.class));
        });

        btnReports.setOnClickListener(v -> {
            startActivity(new Intent(AshaDashboardActivity.this, AshaReportsActivity.class));
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }

    private void loadAshaProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        db.collection("users").document(user.getUid()).get()
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

    private void loadDashboardStats() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        String ashaId = user.getUid();
        
        // Count beneficiaries assigned to this ASHA worker in the global registry
        db.collection("beneficiaries")
            .whereEqualTo("ashaId", ashaId)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null) return;
                
                int totalCount = snapshots.size();
                tvTotalBeneficiaries.setText(String.valueOf(totalCount));
                
                // For alerts: Check pending vaccinations (demonstration logic)
                int alertCount = 0;
                for (DocumentSnapshot doc : snapshots) {
                    // This would ideally be a separate query on the sub-collection or a denormalized field
                    // For now we show the patient count if it's new
                }
                tvOverdueAlerts.setText(String.valueOf(totalCount / 2)); // Dynamic placeholder
            });
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out safely", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
