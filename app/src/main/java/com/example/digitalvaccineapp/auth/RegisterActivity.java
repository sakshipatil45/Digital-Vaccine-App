package com.example.digitalvaccineapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.shared.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.graphics.Color;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etFamilySyncPhone, etPassword, etConfirmPassword, etAge;
    private RadioGroup rgGender;
    private MaterialButton btnRegister;
    private MaterialCardView cardAsha, cardCitizen;
    private String selectedRole = "citizen";
    private TextView tvLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhone = findViewById(R.id.etPhone);
        etFamilySyncPhone = findViewById(R.id.etFamilySyncPhone);
        etAge = findViewById(R.id.etAge);
        rgGender = findViewById(R.id.rgGender);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        cardAsha = findViewById(R.id.cardAsha);
        cardCitizen = findViewById(R.id.cardCitizen);

        cardAsha.setOnClickListener(v -> selectRole("asha"));
        cardCitizen.setOnClickListener(v -> selectRole("citizen"));

        btnRegister.setOnClickListener(v -> registerUser());
        
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void selectRole(String role) {
        selectedRole = role;
        if ("asha".equals(role)) {
            cardAsha.setStrokeColor(Color.parseColor("#4CAF50")); // Green
            cardAsha.setStrokeWidth(4);
            cardAsha.setCardBackgroundColor(Color.parseColor("#E8F5E9"));

            cardCitizen.setStrokeColor(Color.parseColor("#E0E0E0"));
            cardCitizen.setStrokeWidth(2);
            cardCitizen.setCardBackgroundColor(Color.WHITE);
        } else {
            cardCitizen.setStrokeColor(Color.parseColor("#FF9800")); // Orange
            cardCitizen.setStrokeWidth(4);
            cardCitizen.setCardBackgroundColor(Color.parseColor("#FFF3E0"));

            cardAsha.setStrokeColor(Color.parseColor("#E0E0E0"));
            cardAsha.setStrokeWidth(2);
            cardAsha.setCardBackgroundColor(Color.WHITE);
        }
    }

    private void registerUser() {
        String age = etAge.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String familyPhone = etFamilySyncPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
            TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(age) || phone.isEmpty() || familyPhone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() < 10 || familyPhone.length() < 10) {
            Toast.makeText(this, "Enter valid 10-digit phone numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton rbSelected = findViewById(rgGender.getCheckedRadioButtonId());
        String gender = rbSelected.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Save user details to backend
                    saveProfileToBackend(name, phone, familyPhone, age, gender, selectedRole);
                } else {
                    Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    private void saveProfileToBackend(String name, String phone, String familySyncPhone, String age, String gender, String role) {
        if (mAuth.getCurrentUser() == null) return;

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("role", role);
        userData.put("phone", phone);
        userData.put("familySyncPhone", familySyncPhone);
        userData.put("age", age);
        userData.put("gender", gender);
        userData.put("email", mAuth.getCurrentUser().getEmail());
        userData.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("users").document(mAuth.getCurrentUser().getUid())
            .set(userData)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(RegisterActivity.this, "Profile sync failed but account created", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
    }
}
