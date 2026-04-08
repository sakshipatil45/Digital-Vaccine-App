package com.example.digitalvaccineapp.shared;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.shared.Vaccination;
import com.example.digitalvaccineapp.network.VaccinationRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.digitalvaccineapp.core.MockUserManager;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddVaccinationActivity extends AppCompatActivity {

    private TextInputEditText etDateTaken, etHospitalName, etNotes;
    private AutoCompleteTextView spinnerVaccineName, spinnerDoseNumber, spinnerDependentName, spinnerStatus;
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
        spinnerDependentName = findViewById(R.id.spinnerDependentName);
        spinnerStatus = findViewById(R.id.spinnerStatus);
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

        // Set up Status dropdown
        String[] statuses = {"Completed", "Pending"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses);
        spinnerStatus.setAdapter(statusAdapter);
        if (isEditMode) {
             spinnerStatus.setText(getIntent().getStringExtra("vax_status") != null ? getIntent().getStringExtra("vax_status") : "Completed", false);
        } else {
             spinnerStatus.setText("Completed", false);
        }

        // Fetch Family Members for Dependent Dropdown
        List<String> dependents = new ArrayList<>();
        dependents.add("Self");
        
        // Immediate initialization so "Self" is always present instantly
        ArrayAdapter<String> initialAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, dependents);
        spinnerDependentName.setAdapter(initialAdapter);
        
        if (isEditMode) {
            String currentDependent = getIntent().getStringExtra("vax_dependent");
            if (currentDependent == null || currentDependent.isEmpty()) currentDependent = "Self";
            spinnerDependentName.setText(currentDependent, false);
        } else {
            spinnerDependentName.setText("Self", false);
        }
        
        if (MockUserManager.isLoggedIn()) {
            String uid = MockUserManager.getUserId();
            FirebaseFirestore.getInstance().collection("users").document(uid).collection("familyMembers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean addedNew = false;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String depName = doc.getString("name");
                        if (depName != null) {
                            if (!dependents.contains(depName)) {
                                dependents.add(depName);
                            }
                            addedNew = true;
                        }
                    }
                    // Only re-bind adapter if we actually found external dependents
                    if (addedNew) {
                        ArrayAdapter<String> depAdapter = new ArrayAdapter<>(AddVaccinationActivity.this, android.R.layout.simple_dropdown_item_1line, dependents);
                        spinnerDependentName.setAdapter(depAdapter);
                        // Maintain whatever the user (or the initial load) currently has typed/selected
                        String currentText = spinnerDependentName.getText().toString();
                        spinnerDependentName.setText(currentText, false);
                    }
                });
        }
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
            Snackbar.make(findViewById(android.R.id.content), "Please fill all required fields", Snackbar.LENGTH_SHORT).show();
            return;
        }

        int doseNum = 1;
        if (doseStr.contains("2")) doseNum = 2;
        if (doseStr.contains("Booster")) doseNum = 3;

        String dependent = spinnerDependentName.getText().toString().trim();
        if (dependent.isEmpty()) dependent = "Self";

        String status = spinnerStatus.getText().toString();
        if (status.isEmpty()) status = "Completed";

        Vaccination vaccination = new Vaccination(name, doseNum, date, hospital, status, dependent);

        if (isEditMode) {
            updateVaccination(vaccination);
        } else {
            addVaccination(vaccination);
        }
    }

    private void addVaccination(Vaccination vaccination) {
        if (getIntent().getBooleanExtra("asha_beneficiary_mode", false)) {
            String beneficiaryId = getIntent().getStringExtra("beneficiary_id");
            String uid = MockUserManager.getUserId();
            
            if (uid == null || beneficiaryId == null) return;
            
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .collection("beneficiaries").document(beneficiaryId)
                .collection("vaccinations").add(vaccination)
                .addOnSuccessListener(documentReference -> {
                    // Sync with local repository so it shows up on Citizen Dashboard immediately (Mock Connection)
                    repository.addVaccination(vaccination, null);
                    
                    Snackbar.make(findViewById(android.R.id.content), "Vaccination securely linked to Patient", Snackbar.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                     Snackbar.make(findViewById(android.R.id.content), "Error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
            return;
        }

        repository.addVaccination(vaccination, new VaccinationRepository.DataCallback() {
            @Override
            public void onDataLoaded(List<Vaccination> vaccinations) {
                Snackbar.make(findViewById(android.R.id.content), "Vaccination saved securely to Cloud", Snackbar.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Snackbar.make(findViewById(android.R.id.content), "Error: " + message, Snackbar.LENGTH_LONG)
                        .setAction("Retry", v -> saveVaccination()).show();
            }
        });
    }

    private void updateVaccination(Vaccination vaccination) {
        repository.updateVaccination(vaxId, vaccination, new VaccinationRepository.DataCallback() {
            @Override
            public void onDataLoaded(List<Vaccination> vaccinations) {
                Snackbar.make(findViewById(android.R.id.content), "Record updated in Cloud", Snackbar.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Snackbar.make(findViewById(android.R.id.content), "Update failed: " + message, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
