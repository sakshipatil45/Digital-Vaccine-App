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
import com.example.digitalvaccineapp.shared.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.snackbar.Snackbar;

import java.util.UUID;

public class AddFamilyMemberActivity extends AppCompatActivity {

    private TextInputEditText etFamilyName, etFamilyAge;
    private RadioGroup rgFamilyGender;
    private AutoCompleteTextView spFamilyRelationship;
    private MaterialButton btnSaveFamilyMember;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userPhone = "";
    private String userVillage = "";

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

        // Fetch parent's phone for linking
        fetchUserPhone();

        // Setup AutoCompleteTextView
        String[] relationships = {"Child", "Self", "Son", "Daughter", "Father", "Mother", "Spouse", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, relationships);
        spFamilyRelationship.setAdapter(adapter);

        btnSaveFamilyMember.setOnClickListener(v -> saveFamilyMember());
    }

    private void fetchUserPhone() {
        if (mAuth.getCurrentUser() == null) return;
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    userPhone = documentSnapshot.getString("phone");
                    userVillage = documentSnapshot.getString("village");
                }
            });
    }

    private void saveFamilyMember() {
        String name = etFamilyName.getText().toString().trim();
        String age = etFamilyAge.getText().toString().trim();
        String relationship = spFamilyRelationship.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age)) {
            Snackbar.make(findViewById(android.R.id.content), "Please fill all fields", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (userPhone == null || userPhone.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please set your phone number in Profile to sync records", Snackbar.LENGTH_LONG).show();
            return;
        }

        int selectedGenderId = rgFamilyGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Snackbar.make(findViewById(android.R.id.content), "Please select a gender", Snackbar.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender.getText().toString();

        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        // Save to Global Beneficiaries collection for cross-role sync
        btnSaveFamilyMember.setEnabled(false);
        String newId = UUID.randomUUID().toString();
        
        java.util.HashMap<String, Object> memberData = new java.util.HashMap<>();
        memberData.put("id", newId);
        memberData.put("name", name);
        memberData.put("age", age);
        memberData.put("gender", gender);
        memberData.put("category", relationship); // Matching 'category' field used by System Administrators
        memberData.put("mobileNumber", userPhone); // Linking Key
        memberData.put("village", userVillage != null ? userVillage : "General"); // Sync Key
        memberData.put("citizenId", userId);
        memberData.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("beneficiaries")
            .document(newId).set(memberData)
            .addOnSuccessListener(aVoid -> {
                Snackbar.make(findViewById(android.R.id.content), "Record added to shared registry", Snackbar.LENGTH_LONG).show();
                finish();
            })
            .addOnFailureListener(e -> {
                btnSaveFamilyMember.setEnabled(true);
                Snackbar.make(findViewById(android.R.id.content), "Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
            });
    }
}
