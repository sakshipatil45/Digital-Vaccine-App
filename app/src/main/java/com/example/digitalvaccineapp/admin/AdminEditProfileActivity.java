package com.example.digitalvaccineapp.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AdminEditProfileActivity extends AppCompatActivity {
    private TextInputEditText etName, etNewPassword, etConfirmPassword;
    private MaterialButton btnSave;
    private MaterialToolbar toolbar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_profile);

        etName = findViewById(R.id.etEditProfileName);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSave = findViewById(R.id.btnSaveProfile);
        toolbar = findViewById(R.id.toolbarEditProfile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Setup Toolbar back navigation
        toolbar.setNavigationOnClickListener(v -> finish());

        fetchProfile();

        btnSave.setOnClickListener(v -> updateProfile());
    }

    private void fetchProfile() {
        if (mAuth.getCurrentUser() == null) return;
        
        db.collection("users").document(mAuth.getCurrentUser().getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    com.example.digitalvaccineapp.shared.User user = documentSnapshot.toObject(com.example.digitalvaccineapp.shared.User.class);
                    if (user != null) {
                        etName.setText(user.getName());
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error fetching profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateProfile() {
        if (mAuth.getCurrentUser() == null) return;

        String name = etName.getText().toString();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (!newPass.isEmpty()) {
            if (newPass.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            
            mAuth.getCurrentUser().updatePassword(newPass)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Password update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", name);
        userUpdates.put("updatedAt", com.google.firebase.Timestamp.now());

        db.collection("users").document(mAuth.getCurrentUser().getUid())
            .update(userUpdates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                // If update fails because doc doesn't exist, try set
                db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .set(userUpdates)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(this, "Profile created", Toast.LENGTH_SHORT).show();
                        finish();
                    });
            });
    }
}
