package com.example.digitalvaccineapp.citizen;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.citizen.FamilyMember;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.digitalvaccineapp.core.MockUserManager;
import com.example.digitalvaccineapp.asha.Beneficiary;
import com.google.android.material.snackbar.Snackbar;

import java.util.UUID;

public class AddFamilyMemberActivity extends AppCompatActivity {

    private TextInputEditText etFamilyName, etFamilyAge;
    private RadioGroup rgFamilyGender;
    private AutoCompleteTextView spFamilyRelationship;
    private MaterialButton btnSaveFamilyMember;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_family_member);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarAddFamily);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etFamilyName = findViewById(R.id.etFamilyName);
        etFamilyAge = findViewById(R.id.etFamilyAge);
        rgFamilyGender = findViewById(R.id.rgFamilyGender);
        spFamilyRelationship = findViewById(R.id.spFamilyRelationship);
        btnSaveFamilyMember = findViewById(R.id.btnSaveFamilyMember);

        // Setup AutoCompleteTextView
        String[] relationships = {"Child", "Self", "Son", "Daughter", "Father", "Mother", "Spouse", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, relationships);
        spFamilyRelationship.setAdapter(adapter);

        btnSaveFamilyMember.setOnClickListener(v -> saveFamilyMember());
    }

    private void saveFamilyMember() {
        String name = etFamilyName.getText().toString().trim();
        String age = etFamilyAge.getText().toString().trim();
        String relationship = spFamilyRelationship.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age)) {
            Snackbar.make(findViewById(android.R.id.content), "Please fill all fields", Snackbar.LENGTH_SHORT).show();
            return;
        }

        int selectedGenderId = rgFamilyGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Snackbar.make(findViewById(android.R.id.content), "Please select a gender", Snackbar.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender.getText().toString();

        String userId = MockUserManager.getUserId();
        if (userId == null) return;

        // Save to Firestore
        btnSaveFamilyMember.setEnabled(false);
        String newId = UUID.randomUUID().toString();
        FamilyMember member = new FamilyMember(newId, name, age, gender, relationship);

        db.collection("users").document(userId).collection("familyMembers")
            .document(newId).set(member)
            .addOnSuccessListener(aVoid -> {
                // Sync: Automatically create a Beneficiary for the ASHA Health Worker (Mock Collaboration)
                if (MockUserManager.USE_MOCK) {
                    Beneficiary ashaBeneficiary = new Beneficiary(newId, name, age, gender, "Guest Village", "9999999999", relationship, userId);
                    db.collection("users").document(userId).collection("beneficiaries")
                            .document(newId).set(ashaBeneficiary);
                }

                Snackbar.make(findViewById(android.R.id.content), "Family member added successfully", Snackbar.LENGTH_LONG).show();
                finish();
            })
            .addOnFailureListener(e -> {
                btnSaveFamilyMember.setEnabled(true);
                Snackbar.make(findViewById(android.R.id.content), "Failed to add member: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            });
    }
}
