package com.example.digitalvaccineapp.shared;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvName, tvEmail, tvAge, tvGender;
    private MaterialCardView cardEditProfile, cardLogout;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvAge = findViewById(R.id.tvProfileAge);
        tvGender = findViewById(R.id.tvProfileGender);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        cardLogout = findViewById(R.id.cardLogout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            tvEmail.setText(mAuth.getCurrentUser().getEmail());
        }

        cardEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        cardLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            // We should ideally navigate to LoginActivity and clear the top
            Intent intent = new Intent(ProfileActivity.this, com.example.digitalvaccineapp.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fetch profile every time user comes back to this screen
        fetchProfile();
    }

    private void fetchProfile() {
        if (mAuth.getCurrentUser() == null) return;
        
        db.collection("users").document(mAuth.getCurrentUser().getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        tvName.setText(user.getName() != null && !user.getName().isEmpty() ? user.getName() : "User");
                        tvAge.setText(user.getAge() != null && !user.getAge().isEmpty() ? user.getAge() : "Not specified");
                        tvGender.setText(user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : "Not specified");
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ProfileActivity.this, "Error fetching profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
