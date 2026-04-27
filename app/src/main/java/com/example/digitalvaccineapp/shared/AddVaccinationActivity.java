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
import com.example.digitalvaccineapp.citizen.FamilyMember;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddVaccinationActivity extends AppCompatActivity {

    private TextInputEditText etDateTaken, etHospitalName, etNotes;
    private AutoCompleteTextView spinnerVaccineName, spinnerDoseNumber, spinnerDependentName, spinnerStatus;
    private Button btnSave;
    private VaccinationRepository repository;
    private FirebaseFirestore db;
    private boolean isEditMode = false;
    private String vaxId = null;
    private String beneficiaryId = null;
    
    // Maps dependent name -> beneficiaryId for Citizen side
    private Map<String, String> dependentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vaccination);

        db = FirebaseFirestore.getInstance();
        spinnerVaccineName = findViewById(R.id.spinnerVaccineName);
        spinnerDoseNumber = findViewById(R.id.spinnerDoseNumber);
        spinnerDependentName = findViewById(R.id.spinnerDependentName);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        etDateTaken = findViewById(R.id.etDateTaken);
        etHospitalName = findViewById(R.id.etHospitalName);
        etNotes = findViewById(R.id.etNotes);
        btnSave = findViewById(R.id.btnSave);
        repository = new VaccinationRepository(this);

        // Check for Admin mode (passed from BeneficiaryDetailActivity)
        beneficiaryId = getIntent().getStringExtra("beneficiary_id");

        // Check for Edit Mode
        if (getIntent().hasExtra("edit_mode")) {
            isEditMode = getIntent().getBooleanExtra("edit_mode", false);
            vaxId = getIntent().getStringExtra("vax_id");
            beneficiaryId = getIntent().getStringExtra("beneficiary_id");
            
            spinnerVaccineName.setText(getIntent().getStringExtra("vax_name"), false);
            int doseNum = getIntent().getIntExtra("vax_dose", 1);
            String doseStr = doseNum == 1 ? "1st Dose" : (doseNum == 2 ? "2nd Dose" : "Booster Dose");
            spinnerDoseNumber.setText(doseStr, false);
            etDateTaken.setText(getIntent().getStringExtra("vax_date"));
            etHospitalName.setText(getIntent().getStringExtra("vax_hospital"));
            
            btnSave.setText("Update Vaccination");
        }

        // Set up drop downs...
        setupSpinners();

        // Date picker
        etDateTaken.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveVaccination());

        // Fetch Dependents (Family Members) to link by ID
        loadDependents();
    }

    private void setupSpinners() {
        // Fetch from Master Collection
        db.collection("vaccines_master").get().addOnSuccessListener(snapshots -> {
            List<String> vaccineNames = new ArrayList<>();
            if (snapshots.isEmpty()) {
                // Fallback
                String[] vaccines = {"Covaxin", "Covishield", "Sputnik V", "Pfizer", "Moderna", "Other"};
                for(String s : vaccines) vaccineNames.add(s);
            } else {
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshots) {
                    String name = doc.getString("name");
                    if (name != null) vaccineNames.add(name);
                }
                vaccineNames.add("Other");
            }
            ArrayAdapter<String> vaxAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vaccineNames);
            spinnerVaccineName.setAdapter(vaxAdapter);
        });

        String[] doses = {"1st Dose", "2nd Dose", "Booster Dose", "Precautionary Dose"};
        spinnerDoseNumber.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, doses));

        String[] statuses = {"Completed", "Pending"};
        spinnerStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses));
        spinnerStatus.setText("Completed", false);
    }

    private void loadDependents() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // If ASHA passed a beneficiary, we force it
        String forcedName = getIntent().getStringExtra("force_dependent");
        if (forcedName != null) {
            spinnerDependentName.setText(forcedName, false);
            spinnerDependentName.setEnabled(false);
            return;
        }

        // Otherwise, fetch from global registry using userId
        db.collection("family_members").whereEqualTo("userId", uid).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> dependentNames = new ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String name = doc.getString("name");
                    if (name != null) {
                        dependentNames.add(name);
                        dependentMap.put(name, doc.getId());
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, dependentNames);
                spinnerDependentName.setAdapter(adapter);
            });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", dayOfMonth);
            etDateTaken.setText(date);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void saveVaccination() {
        String name = spinnerVaccineName.getText().toString();
        String doseStr = spinnerDoseNumber.getText().toString();
        String date = etDateTaken.getText().toString();
        String hospital = etHospitalName.getText().toString();
        String depName = spinnerDependentName.getText().toString();

        if (name.isEmpty() || date.isEmpty() || depName.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "All fields required", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Resolve beneficiaryId if in Citizen mode
        if (beneficiaryId == null) {
            beneficiaryId = dependentMap.get(depName);
        }

        if (beneficiaryId == null) {
             Snackbar.make(findViewById(android.R.id.content), "Error: Beneficiary ID not found", Snackbar.LENGTH_SHORT).show();
             return;
        }

        int doseNum = doseStr.contains("2") ? 2 : (doseStr.contains("Booster") ? 3 : 1);
        Vaccination vax = new Vaccination(name, doseNum, date, hospital, spinnerStatus.getText().toString(), depName);

        btnSave.setEnabled(false);
        if (isEditMode) {
            repository.updateVaccination(beneficiaryId, vaxId, vax, new VaccinationRepository.DataCallback() {
                @Override public void onDataLoaded(List<Vaccination> v) { 
                    finish(); 
                }
                @Override public void onError(String msg) { btnSave.setEnabled(true); Snackbar.make(btnSave, msg, 2000).show(); }
            });
        }
    }
}
