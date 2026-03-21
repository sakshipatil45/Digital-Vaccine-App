package com.example.digitalvaccineapp.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.models.FamilyMember;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AddFamilyMemberActivity extends AppCompatActivity {

    private TextInputEditText etFamilyName, etFamilyAge;
    private RadioGroup rgFamilyGender;
    private Spinner spFamilyRelationship;
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

        // Setup Spinner
        String[] relationships = {"Son", "Daughter", "Father", "Mother", "Spouse", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, relationships);
        spFamilyRelationship.setAdapter(adapter);

        btnSaveFamilyMember.setOnClickListener(v -> saveFamilyMember());
    }

    private void saveFamilyMember() {
        String name = etFamilyName.getText().toString().trim();
        String age = etFamilyAge.getText().toString().trim();
        String relationship = spFamilyRelationship.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedGenderId = rgFamilyGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender.getText().toString();

        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        // Save to Firestore
        btnSaveFamilyMember.setEnabled(false);
        String newId = UUID.randomUUID().toString();
        FamilyMember member = new FamilyMember(newId, name, age, gender, relationship);

        db.collection("users").document(userId).collection("familyMembers")
            .document(newId).set(member)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Family member added successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                btnSaveFamilyMember.setEnabled(true);
                Toast.makeText(this, "Failed to add member: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
