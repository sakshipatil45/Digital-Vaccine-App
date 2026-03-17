package com.example.digitalvaccineapp.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.models.ApiResponse;
import com.example.digitalvaccineapp.models.Vaccination;
import com.example.digitalvaccineapp.network.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddVaccinationActivity extends AppCompatActivity {

    private TextInputEditText etDateTaken, etHospitalName, etNotes;
    private AutoCompleteTextView spinnerVaccineName, spinnerDoseNumber;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vaccination);

        spinnerVaccineName = findViewById(R.id.spinnerVaccineName);
        spinnerDoseNumber = findViewById(R.id.spinnerDoseNumber);
        etDateTaken = findViewById(R.id.etDateTaken);
        etHospitalName = findViewById(R.id.etHospitalName);
        etNotes = findViewById(R.id.etNotes);
        btnSave = findViewById(R.id.btnSave);

        // Set up Vaccine Name dropdown
        String[] vaccines = {"Covaxin", "Covishield", "Sputnik V", "Pfizer", "Moderna", "Johnson & Johnson", "Other"};
        ArrayAdapter<String> vaccineAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vaccines);
        spinnerVaccineName.setAdapter(vaccineAdapter);

        // Set up Dose Number dropdown
        String[] doses = {"1st Dose", "2nd Dose", "Booster Dose", "Precautionary Dose"};
        ArrayAdapter<String> doseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, doses);
        spinnerDoseNumber.setAdapter(doseAdapter);

        // Date picker
        etDateTaken.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveVaccination());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            String date = year1 + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", dayOfMonth);
            etDateTaken.setText(date);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void saveVaccination() {
        String name = spinnerVaccineName.getText().toString();
        String doseStr = spinnerDoseNumber.getText().toString();
        String date = etDateTaken.getText().toString();
        String hospital = etHospitalName.getText().toString();
        String notes = etNotes.getText().toString();

        if (name.isEmpty() || doseStr.isEmpty() || date.isEmpty() || hospital.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert Dose String back to number representation for backend compatibility or change backend
        int doseNum = 1;
        if (doseStr.contains("2")) doseNum = 2;
        if (doseStr.contains("Booster")) doseNum = 3;

        // Using default fields for Dependent and Status if not specified in UI
        Vaccination vaccination = new Vaccination(name, doseNum, date, hospital, "Completed", "Self");
        // We might want to pass notes back, but keeping existing model for now to avoid breaking backend

        RetrofitClient.getApiService().addVaccination(vaccination).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddVaccinationActivity.this, "Vaccination saved securely", Toast.LENGTH_SHORT).show();
                    // Redirect to view records
                    Intent intent = new Intent(AddVaccinationActivity.this, RecordsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AddVaccinationActivity.this, "Failed to add vaccination", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(AddVaccinationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
