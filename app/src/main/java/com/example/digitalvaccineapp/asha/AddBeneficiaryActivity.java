package com.example.digitalvaccineapp.asha;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.asha.Beneficiary;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AddBeneficiaryActivity extends AppCompatActivity {

    private TextInputEditText etName, etAge, etVillage, etMobile;
    private RadioGroup rgGender;
    private Spinner spCategory;
    private MaterialButton btnSave;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean isEditMode = false;
    private String editBeneficiaryId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_beneficiary);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarAddBeneficiary);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.etBeneficiaryName);
        etAge = findViewById(R.id.etBeneficiaryAge);
        etVillage = findViewById(R.id.etBeneficiaryVillage);
        etMobile = findViewById(R.id.etBeneficiaryMobile);
        rgGender = findViewById(R.id.rgBeneficiaryGender);
        spCategory = findViewById(R.id.spBeneficiaryCategory);
        btnSave = findViewById(R.id.btnSaveBeneficiary);

        String[] categories = {"Child", "Pregnant Woman", "Adult"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        spCategory.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveBeneficiary());

        if (getIntent().hasExtra("edit_mode")) {
            isEditMode = getIntent().getBooleanExtra("edit_mode", false);
            editBeneficiaryId = getIntent().getStringExtra("beneficiaryId");
            btnSave.setText("Update Patient Data");
            loadExistingData();
        }
    }

    private void loadExistingData() {
        if (mAuth.getCurrentUser() == null || editBeneficiaryId == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("beneficiaries").document(editBeneficiaryId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    etName.setText(documentSnapshot.getString("name"));
                    etAge.setText(documentSnapshot.getString("age"));
                    etVillage.setText(documentSnapshot.getString("village"));
                    etMobile.setText(documentSnapshot.getString("mobileNumber"));
                    
                    String gender = documentSnapshot.getString("gender");
                    if ("Male".equalsIgnoreCase(gender)) rgGender.check(R.id.rbMale);
                    else if ("Female".equalsIgnoreCase(gender)) rgGender.check(R.id.rbFemale);
                    
                    String category = documentSnapshot.getString("category");
                    if (category != null) {
                        String[] categories = {"Child", "Pregnant Woman", "Adult"};
                        for (int i = 0; i < categories.length; i++) {
                            if (categories[i].equals(category)) {
                                spCategory.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            })
            .addOnFailureListener(e -> {
                Snackbar.make(findViewById(android.R.id.content), "Error loading data: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
            });
    }

    private void saveBeneficiary() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String village = etVillage.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();

        if (name.isEmpty() || age.isEmpty() || village.isEmpty() || mobile.isEmpty() || category.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "All fields must be filled.", Snackbar.LENGTH_SHORT).show();
            return;
        }
        
        if (mobile.length() < 10) {
            Snackbar.make(findViewById(android.R.id.content), "Enter a valid 10-digit mobile number.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Snackbar.make(findViewById(android.R.id.content), "Please select a gender.", Snackbar.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender.getText().toString();

        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        String ashaId = userId;

        btnSave.setEnabled(false);
        
        String beneficiaryId;
        if (isEditMode) {
            beneficiaryId = editBeneficiaryId;
        } else {
            beneficiaryId = UUID.randomUUID().toString();
        }

        Beneficiary beneficiary = new Beneficiary(beneficiaryId, name, age, gender, village, mobile, category, ashaId);

        if (isEditMode) {
            db.collection("users").document(userId).collection("beneficiaries").document(editBeneficiaryId)
                .set(beneficiary)
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(findViewById(android.R.id.content), "Patient data updated", Snackbar.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), "Update failed: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                });
        } else {
            db.collection("users").document(userId).collection("beneficiaries")
                .document(beneficiaryId)
                .set(beneficiary)
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(findViewById(android.R.id.content), "Beneficiary successfully registered", Snackbar.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                });
        }
    }
}
