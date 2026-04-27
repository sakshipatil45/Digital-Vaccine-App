package com.example.digitalvaccineapp.auth;

import com.example.digitalvaccineapp.citizen.VaccinationActivity;
import com.example.digitalvaccineapp.admin.AdminDashboardActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalvaccineapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String phoneInput = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phoneInput) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter mobile number and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Faking email for Firebase Auth
        String email = phoneInput;
        if (!email.contains("@")) {
            email = email + "@digitalvaccine.com";
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        db.collection("users").document(userId).get()
                                .addOnCompleteListener(roleTask -> {
                                    if (roleTask.isSuccessful() && roleTask.getResult() != null) {
                                        DocumentSnapshot document = roleTask.getResult();
                                        String role = document.getString("role");

                                        // Save FCM Token for Cloud Push Notifications
                                        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                                                .addOnSuccessListener(token -> {
                                                    db.collection("users").document(userId).update("fcmToken", token);
                                                });

                                        // Save login session (SharedPreferences)
                                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putBoolean("isLoggedIn", true);
                                        editor.putString("userRole", role != null ? role : "citizen");
                                        editor.apply();

                                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT)
                                                .show();
                                        Intent intent;
                                        if ("admin".equals(role)) {
                                            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                        } else {
                                            intent = new Intent(LoginActivity.this, VaccinationActivity.class);
                                        }
                                        intent.setFlags(
                                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Failed to load profile data",
                                                Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                    }
                                });
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
