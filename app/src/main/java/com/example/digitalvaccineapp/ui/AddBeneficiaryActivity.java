package com.example.digitalvaccineapp.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.models.Beneficiary;
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spCategory.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveBeneficiary());
    }

    private void saveBeneficiary() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String village = etVillage.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age) || TextUtils.isEmpty(village)) {
            Snackbar.make(findViewById(android.R.id.content), "Please fill all essential fields (Name, Age, Village)", Snackbar.LENGTH_SHORT).show();
            return;
        }

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Snackbar.make(findViewById(android.R.id.content), "Please select a gender", Snackbar.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender.getText().toString();

        if (mAuth.getCurrentUser() == null) return;
        String ashaId = mAuth.getCurrentUser().getUid();

        btnSave.setEnabled(false);
        String newId = UUID.randomUUID().toString();
        Beneficiary beneficiary = new Beneficiary(newId, name, age, gender, village, mobile, category, ashaId);

        db.collection("users").document(ashaId).collection("beneficiaries")
            .document(newId).set(beneficiary)
            .addOnSuccessListener(aVoid -> {
                Snackbar.make(findViewById(android.R.id.content), "Beneficiary securely onboarded", Snackbar.LENGTH_LONG).show();
                finish();
            })
            .addOnFailureListener(e -> {
                btnSave.setEnabled(true);
                Snackbar.make(findViewById(android.R.id.content), "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            });
    }
}
