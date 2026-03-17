package com.example.digitalvaccineapp.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.models.ApiResponse;
import com.example.digitalvaccineapp.models.User;
import com.example.digitalvaccineapp.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {
    private TextInputEditText etName, etEmail, etAge;
    private AutoCompleteTextView etGender;
    private MaterialButton btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.etProfileName);
        etEmail = findViewById(R.id.etProfileEmail);
        etAge = findViewById(R.id.etProfileAge);
        etGender = findViewById(R.id.etProfileGender);
        btnUpdate = findViewById(R.id.btnUpdateProfile);
        
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
        RetrofitClient.getApiService().getProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getData();
                    if (user != null) {
                        etName.setText(user.getName());
                        etAge.setText(user.getAge());
                        if (user.getGender() != null) {
                            etGender.setText(user.getGender(), false); // false to not trigger filter
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error fetching profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        String name = etName.getText().toString();
        String age = etAge.getText().toString();
        String gender = etGender.getText().toString();

        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setGender(gender);

        RetrofitClient.getApiService().updateProfile(user).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
