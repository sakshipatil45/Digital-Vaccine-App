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

public class ProfileActivity extends AppCompatActivity {
    private TextInputEditText etName, etPhone, etAge, etAddress;
    private MaterialButton btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.etProfileName);
        etPhone = findViewById(R.id.etProfilePhone);
        etAge = findViewById(R.id.etProfileAge);
        etAddress = findViewById(R.id.etProfileAddress);
        btnUpdate = findViewById(R.id.btnUpdateProfile);

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
                        etPhone.setText(user.getPhone());
                        etAge.setText(user.getAge());
                        etAddress.setText(user.getAddress());
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
        String phone = etPhone.getText().toString();
        String age = etAge.getText().toString();
        String address = etAddress.getText().toString();

        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setAge(age);
        user.setAddress(address);

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
