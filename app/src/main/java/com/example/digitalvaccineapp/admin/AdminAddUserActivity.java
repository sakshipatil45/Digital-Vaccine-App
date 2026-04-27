package com.example.digitalvaccineapp.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.admin.Beneficiary;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AdminAddUserActivity extends AppCompatActivity {

    private TextInputEditText etName, etMobile;
    private MaterialButton btnSave;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean isEditMode = false;
    private String editUserId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_user);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarAddBeneficiary);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Update User" : "Add New User");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.etUserName);
        etMobile = findViewById(R.id.etUserMobile);
        btnSave = findViewById(R.id.btnSaveUser);

        btnSave.setOnClickListener(v -> saveUser());

        if (getIntent().hasExtra("edit_mode")) {
            isEditMode = getIntent().getBooleanExtra("edit_mode", false);
            editUserId = getIntent().getStringExtra("userId");
            btnSave.setText("Update User Data");
            loadExistingData();
        }
    }

    private void loadExistingData() {
        if (editUserId == null) return;
        
        db.collection("users").document(editUserId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    etName.setText(documentSnapshot.getString("name"));
                    etMobile.setText(documentSnapshot.getString("phone"));
                }
            })
            .addOnFailureListener(e -> {
                Snackbar.make(findViewById(android.R.id.content), "Error loading data: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
            });
    }

    private void saveUser() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();

        if (name.isEmpty() || mobile.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "All fields must be filled.", Snackbar.LENGTH_SHORT).show();
            return;
        }
        
        if (mobile.length() < 10) {
            Snackbar.make(findViewById(android.R.id.content), "Enter a valid 10-digit mobile number.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) return;

        btnSave.setEnabled(false);
        
        String userId;
        if (isEditMode) {
            userId = editUserId;
        } else {
            userId = UUID.randomUUID().toString();
        }

        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("userId", userId);
        userData.put("name", name);
        userData.put("phone", mobile);
        userData.put("role", "Citizen");
        userData.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener(aVoid -> {
                Snackbar.make(findViewById(android.R.id.content), isEditMode ? "User data updated" : "User successfully registered", Snackbar.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                btnSave.setEnabled(true);
                Snackbar.make(findViewById(android.R.id.content), "Operation failed: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
            });
    }
}
