package com.example.digitalvaccineapp.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {
    private TextInputEditText etName, etEmail, etAge;
    private AutoCompleteTextView etGender;
    private MaterialButton btnUpdate;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.etProfileName);
        etEmail = findViewById(R.id.etProfileEmail);
        etAge = findViewById(R.id.etProfileAge);
        etGender = findViewById(R.id.etProfileGender);
        btnUpdate = findViewById(R.id.btnUpdateProfile);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        // Setup Gender Dropdown
        String[] genders = {"Male", "Female", "Other", "Prefer not to say"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        etGender.setAdapter(adapter);

        // Fetch auth email for read-only display
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            etEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }

        fetchProfile();

        btnUpdate.setOnClickListener(v -> updateProfile());
    }

    private void fetchProfile() {
        if (mAuth.getCurrentUser() == null) return;
        
        db.collection("users").document(mAuth.getCurrentUser().getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        etName.setText(user.getName());
                        etAge.setText(user.getAge());
                        if (user.getGender() != null) {
                            etGender.setText(user.getGender(), false);
                        }
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ProfileActivity.this, "Error fetching profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void updateProfile() {
        if (mAuth.getCurrentUser() == null) return;

        String name = etName.getText().toString();
        String age = etAge.getText().toString();
        String gender = etGender.getText().toString();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", name);
        userUpdates.put("age", age);
        userUpdates.put("gender", gender);
        userUpdates.put("updatedAt", com.google.firebase.Timestamp.now());

        db.collection("users").document(mAuth.getCurrentUser().getUid())
            .update(userUpdates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                // If update fails because doc doesn't exist, try set
                db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .set(userUpdates)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(ProfileActivity.this, "Profile created", Toast.LENGTH_SHORT).show();
                        finish();
                    });
            });
    }
}
