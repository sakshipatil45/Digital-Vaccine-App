package com.example.digitalvaccineapp.shared;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.network.VaccinationRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReminderActivity extends AppCompatActivity {

    private AutoCompleteTextView spinnerPatient, spinnerVaccine;
    private TextInputEditText etDate;
    private MaterialButton btnSetReminder;
    private Calendar selectedCalendar;
    
    private FirebaseFirestore db;
    private String selectedBeneficiaryId = null;
    private Map<String, String> patientMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        db = FirebaseFirestore.getInstance();
        createNotificationChannel();

        spinnerPatient = findViewById(R.id.spinnerReminderPatient);
        spinnerVaccine = findViewById(R.id.spinnerReminderVaccine);
        etDate = findViewById(R.id.etReminderDate);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        
        requestNotificationPermission();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Setup Vaccines
        String[] vaccines = {"Covaxin", "Covishield", "Sputnik V", "Pfizer", "Moderna", "Other"};
        spinnerVaccine.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vaccines));

        // Setup Patients (Global Registry Sync)
        loadPatients();

        etDate.setOnClickListener(v -> showDatePicker());
        
        btnSetReminder.setOnClickListener(v -> setReminder());
    }

    private void loadPatients() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 1. Get User Profile to check role/phone
        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            String role = userDoc.getString("role");
            String phone = userDoc.getString("phone");

            if ("asha".equalsIgnoreCase(role)) {
                // Fetch all beneficiaries assigned to this ASHA
                db.collection("beneficiaries").whereEqualTo("ashaId", uid).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> populateSpinner(queryDocumentSnapshots));
            } else {
                // Fetch family members linked by phone
                if (phone != null) {
                    db.collection("beneficiaries").whereEqualTo("mobileNumber", phone).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> populateSpinner(queryDocumentSnapshots));
                }
            }
        });
    }

    private void populateSpinner(com.google.firebase.firestore.QuerySnapshot snapshots) {
        List<String> names = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshots) {
            String name = doc.getString("name");
            if (name != null) {
                names.add(name);
                patientMap.put(name, doc.getId());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
        spinnerPatient.setAdapter(adapter);
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(Calendar.YEAR, year);
            selectedCalendar.set(Calendar.MONTH, month);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            selectedCalendar.set(Calendar.HOUR_OF_DAY, 9);
            selectedCalendar.set(Calendar.MINUTE, 0);

            String date = year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", dayOfMonth);
            etDate.setText(date);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void setReminder() {
        String patientName = spinnerPatient.getText().toString();
        String vaccine = spinnerVaccine.getText().toString();
        String date = etDate.getText().toString();
        
        selectedBeneficiaryId = patientMap.get(patientName);

        if (selectedBeneficiaryId == null || vaccine.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all fields for shared sync", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSetReminder.setEnabled(false);

        // SYNC WITH FIRESTORE (Phase 2 - Shared Registry)
        VaccinationRepository repo = new VaccinationRepository(this);
        repo.addReminder(selectedBeneficiaryId, vaccine, date, new VaccinationRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                scheduleLocalNotification(vaccine);
                Toast.makeText(ReminderActivity.this, "Shared reminder synced and scheduled!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String message) {
                btnSetReminder.setEnabled(true);
                Toast.makeText(ReminderActivity.this, "Cloud sync failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scheduleLocalNotification(String vaccine) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("vaccineName", vaccine);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, vaccine.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null && selectedCalendar != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, selectedCalendar.getTimeInMillis(), pendingIntent);
            // Also save locally for UI history
            NotificationPrefs.saveReminder(this, "Vaccine Reminder", "Next dose: " + vaccine, etDate.getText().toString());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("VAX_REMINDERS", "Vaccine Reminders", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}
