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

    private TextInputEditText etFamilyName, etFamilyAge, etUserPhone, etMotherName, etFatherName, etHusbandName;
    private RadioGroup rgFamilyGender, rgFamilyPregnant;
    private android.widget.LinearLayout layoutChildFields, layoutPregnantFields;
    private AutoCompleteTextView spinnerCategory;
    private MaterialButton btnSaveFamilyMember;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseAuth secondaryAuth;
    private boolean isEditMode = false;
    private String editMemberId = null;
    private String existingUserId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_family_member);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize secondary auth for background account creation
        try {
            com.google.firebase.FirebaseOptions options = com.google.firebase.FirebaseApp.getInstance().getOptions();
            com.google.firebase.FirebaseApp secondaryApp = com.google.firebase.FirebaseApp.initializeApp(this, options, "Secondary");
            secondaryAuth = com.google.firebase.auth.FirebaseAuth.getInstance(secondaryApp);
        } catch (Exception e) {
            secondaryAuth = mAuth; // Fallback
        }

        Toolbar toolbar = findViewById(R.id.toolbarAddFamily);
        setSupportActionBar(toolbar);
        
        if (getIntent().hasExtra("edit_mode")) {
            isEditMode = getIntent().getBooleanExtra("edit_mode", false);
            editMemberId = getIntent().getStringExtra("beneficiaryId");
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Patient Profile" : "Add Family Member");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etFamilyName = findViewById(R.id.etFamilyName);
        etFamilyAge = findViewById(R.id.etFamilyAge);
        etUserPhone = findViewById(R.id.etUserPhone);
        etMotherName = findViewById(R.id.etMotherName);
        etFatherName = findViewById(R.id.etFatherName);
        etHusbandName = findViewById(R.id.etHusbandName);
        rgFamilyGender = findViewById(R.id.rgFamilyGender);
        rgFamilyPregnant = findViewById(R.id.rgFamilyPregnant);
        layoutChildFields = findViewById(R.id.layoutChildFields);
        layoutPregnantFields = findViewById(R.id.layoutPregnantFields);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSaveFamilyMember = findViewById(R.id.btnSaveFamilyMember);

        setupCategorySpinner();

        if (isEditMode) {
            btnSaveFamilyMember.setText("Update Record");
            loadExistingData();
        }

        btnSaveFamilyMember.setOnClickListener(v -> saveFamilyMember());
    }

    private void loadExistingData() {
        if (editMemberId == null) return;
        db.collection("family_members").document(editMemberId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    existingUserId = doc.getString("userId");
                    etFamilyName.setText(doc.getString("name"));
                    etFamilyAge.setText(doc.getString("age"));
                    
                    String category = doc.getString("category");
                    if (category != null) {
                        spinnerCategory.setText(category, false);
                        updateFieldVisibility(category);
                    }
                    
                    String gender = doc.getString("gender");
                    if ("Male".equalsIgnoreCase(gender)) rgFamilyGender.check(R.id.rbFamilyMale);
                    else if ("Female".equalsIgnoreCase(gender)) rgFamilyGender.check(R.id.rbFamilyFemale);

                    etMotherName.setText(doc.getString("motherName"));
                    etFatherName.setText(doc.getString("fatherName"));
                    etHusbandName.setText(doc.getString("husbandName"));

                    if (existingUserId != null) {
                        db.collection("users").document(existingUserId).get()
                            .addOnSuccessListener(userDoc -> {
                                if (userDoc.exists()) {
                                    etUserPhone.setText(userDoc.getString("phone"));
                                }
                            });
                    }
                }
            });
    }

    private void setupCategorySpinner() {
        String[] categories = {"0–1 year", "1–5 years", "6–12 years", "Pregnant Women", "18+ years"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selected = parent.getItemAtPosition(position).toString();
            updateFieldVisibility(selected);
        });
    }

    private void updateFieldVisibility(String category) {
        layoutChildFields.setVisibility(android.view.View.GONE);
        layoutPregnantFields.setVisibility(android.view.View.GONE);
        rgFamilyPregnant.setVisibility(android.view.View.GONE);

        if (category.equals("Pregnant Women")) {
            layoutPregnantFields.setVisibility(android.view.View.VISIBLE);
            rgFamilyPregnant.check(R.id.rbPregnantYes);
            rgFamilyGender.check(R.id.rbFamilyFemale);
        } else if (category.contains("year")) {
            layoutChildFields.setVisibility(android.view.View.VISIBLE);
            rgFamilyPregnant.check(R.id.rbPregnantNo);
        }
    }

    private void saveFamilyMember() {
        String name = etFamilyName.getText().toString().trim();
        String ageStr = etFamilyAge.getText().toString().trim();
        String targetPhone = etUserPhone.getText().toString().trim();
        String category = spinnerCategory.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(targetPhone) || TextUtils.isEmpty(category)) {
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

        final boolean isPregnant = (category.equals("Pregnant Women"));

        btnSaveFamilyMember.setEnabled(false);

        // Find User by Phone
        db.collection("users").whereEqualTo("phone", targetPhone).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Auto-create user account
                        String email = targetPhone + "@digitalvaccine.com";
                        secondaryAuth.createUserWithEmailAndPassword(email, "password123")
                            .addOnSuccessListener(authResult -> {
                                String newUserId = authResult.getUser().getUid();
                                java.util.HashMap<String, Object> newUser = new java.util.HashMap<>();
                                newUser.put("userId", newUserId);
                                newUser.put("phone", targetPhone);
                                newUser.put("name", "User " + targetPhone);
                                newUser.put("role", "citizen");
                                newUser.put("role", "citizen");
                                newUser.put("createdAt", com.google.firebase.Timestamp.now());

                                db.collection("users").document(newUserId).set(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        saveMemberToDb(newUserId, targetPhone, name, ageStr, gender, isPregnant, category);
                                    });
                                
                                secondaryAuth.signOut();
                            })
                            .addOnFailureListener(e -> {
                                btnSaveFamilyMember.setEnabled(true);
                                Snackbar.make(findViewById(android.R.id.content), "Auto-reg failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            });
                        return;
                    }

                    com.google.firebase.firestore.DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                    String targetUserId = userDoc.getId();

                    saveMemberToDb(targetUserId, targetPhone, name, ageStr, gender, isPregnant, category);
                })
                .addOnFailureListener(e -> {
                    btnSaveFamilyMember.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), "Search error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    private void saveMemberToDb(String targetUserId, String targetPhone, String name, String ageStr, String gender, boolean isPregnant, String category) {
        String docId = isEditMode && editMemberId != null ? editMemberId : UUID.randomUUID().toString();
        java.util.HashMap<String, Object> memberData = new java.util.HashMap<>();
        if (!isEditMode) {
            memberData.put("memberId", docId);
        }
        memberData.put("targetPhone", targetPhone);
        memberData.put("userId", targetUserId);

        memberData.put("name", name);
        memberData.put("age", ageStr);
        memberData.put("gender", gender);
        memberData.put("isPregnant", isPregnant);
        memberData.put("category", category);
        memberData.put("createdAt", com.google.firebase.Timestamp.now());
        memberData.put("updatedAt", com.google.firebase.Timestamp.now());

        if (category.contains("year")) {
            memberData.put("motherName", etMotherName.getText().toString().trim());
            memberData.put("fatherName", etFatherName.getText().toString().trim());
            memberData.put("husbandName", null); // Clear if switched category
        } else if (category.equals("Pregnant Women")) {
            memberData.put("husbandName", etHusbandName.getText().toString().trim());
            memberData.put("motherName", null);
            memberData.put("fatherName", null);
        }

        if (isEditMode) {
            db.collection("family_members").document(docId).update(memberData)
                .addOnSuccessListener(aVoid -> {
                    btnSaveFamilyMember.setEnabled(true);
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSaveFamilyMember.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), "Update failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
        } else {
            db.collection("family_members").document(docId).set(memberData)
                .addOnSuccessListener(aVoid -> {
                    btnSaveFamilyMember.setEnabled(true);
                    Toast.makeText(this, "Member added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSaveFamilyMember.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content), "Save failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
        }
    }
}
