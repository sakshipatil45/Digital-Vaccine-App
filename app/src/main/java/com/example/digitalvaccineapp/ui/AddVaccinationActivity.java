package com.example.digitalvaccineapp.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.models.Vaccination;
import com.example.digitalvaccineapp.network.VaccinationRepository;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;
import java.util.List;

public class AddVaccinationActivity extends AppCompatActivity {

    private TextInputEditText etDateTaken, etHospitalName, etNotes;
    private AutoCompleteTextView spinnerVaccineName, spinnerDoseNumber;
    private Button btnSave;
    private VaccinationRepository repository;
    private boolean isEditMode = false;
    private String vaxId = null;

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
        repository = new VaccinationRepository(this);

        // Check for Edit Mode
        if (getIntent().hasExtra("edit_mode")) {
            isEditMode = getIntent().getBooleanExtra("edit_mode", false);
            vaxId = getIntent().getStringExtra("vax_id");
            
            spinnerVaccineName.setText(getIntent().getStringExtra("vax_name"), false);
            int doseNum = getIntent().getIntExtra("vax_dose", 1);
            String doseStr = doseNum == 1 ? "1st Dose" : (doseNum == 2 ? "2nd Dose" : "Booster Dose");
            spinnerDoseNumber.setText(doseStr, false);
            etDateTaken.setText(getIntent().getStringExtra("vax_date"));
            etHospitalName.setText(getIntent().getStringExtra("vax_hospital"));
            
            btnSave.setText("Update Vaccination");
        }

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

        if (name.isEmpty() || doseStr.isEmpty() || date.isEmpty() || hospital.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int doseNum = 1;
        if (doseStr.contains("2")) doseNum = 2;
        if (doseStr.contains("Booster")) doseNum = 3;

        Vaccination vaccination = new Vaccination(name, doseNum, date, hospital, "Completed", "Self");

        if (isEditMode) {
            updateVaccination(vaccination);
        } else {
            addVaccination(vaccination);
        }
    }

    private void addVaccination(Vaccination vaccination) {
        repository.addVaccination(vaccination, new VaccinationRepository.DataCallback() {
            @Override
            public void onDataLoaded(List<Vaccination> vaccinations) {
                Toast.makeText(AddVaccinationActivity.this, "Vaccination saved securely to Cloud", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AddVaccinationActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateVaccination(Vaccination vaccination) {
        repository.updateVaccination(vaxId, vaccination, new VaccinationRepository.DataCallback() {
            @Override
            public void onDataLoaded(List<Vaccination> vaccinations) {
                Toast.makeText(AddVaccinationActivity.this, "Record updated in Cloud", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AddVaccinationActivity.this, "Update failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
