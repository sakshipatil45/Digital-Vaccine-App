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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.Map;

// CLEANED UP VERSION
public class ReminderActivity extends AppCompatActivity {

    private AutoCompleteTextView spinnerVaccine;
    private TextInputEditText etDate, etTime, etPlace;
    private MaterialButton btnSetReminder;
    private java.util.Calendar selectedCalendar;
    
    private VaccinationRepository repository;
    private com.google.firebase.firestore.FirebaseFirestore db;
    private AutoCompleteTextView spinnerCategory;
    private String userRole = "citizen";
    private String userPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        db = FirebaseFirestore.getInstance();
        repository = new VaccinationRepository(this);
        createNotificationChannel();

        spinnerVaccine = findViewById(R.id.spinnerReminderVaccine);
        spinnerCategory = findViewById(R.id.spinnerReminderCategory);
        etDate = findViewById(R.id.etReminderDate);
        etTime = findViewById(R.id.etReminderTime);
        etPlace = findViewById(R.id.etReminderPlace);
        btnSetReminder = findViewById(R.id.btnSetReminder);

        String[] categories = {"0–1 year", "1–5 years", "6–12 years", "Pregnant Women", "18+ years"};
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories));
        spinnerCategory.setThreshold(0);
        
        spinnerVaccine.setThreshold(0);
        setupVaccineAdapter();

        etDate.setOnClickListener(v -> showDatePicker());
        btnSetReminder.setOnClickListener(v -> setReminder());
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        requestNotificationPermission();
        loadUserProfile();
    }

    private void loadUserProfile() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                userRole = doc.getString("role");
                userPhone = doc.getString("phone");
            }
        });
    }

    private void setupVaccineAdapter() {
        String[] vaccines = {"Covaxin", "Covishield", "Sputnik V", "Pfizer", "Moderna", "Other"};
        spinnerVaccine.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vaccines));
        
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("force_vaccine")) {
            spinnerVaccine.setText(intent.getStringExtra("force_vaccine"), false);
        }
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
        String vaccine = spinnerVaccine.getText().toString();
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();
        String place = etPlace.getText().toString();

        if (vaccine.isEmpty() || date.isEmpty() || time.isEmpty() || place.isEmpty()) {
            Toast.makeText(this, "Please fill all fields (Date, Time, Place)", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSetReminder.setEnabled(false);

        String category = spinnerCategory.getText().toString();
        if (category.isEmpty()) {
            Toast.makeText(this, "Please select an age group", Toast.LENGTH_SHORT).show();
            btnSetReminder.setEnabled(true);
            return;
        }

        if ("admin".equalsIgnoreCase(userRole)) {
            // Admins create global campaigns AND individual pending records
            repository.scheduleGlobalVaccination(category, vaccine, date, time, place, new VaccinationRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    scheduleLocalNotification(vaccine);
                    Toast.makeText(ReminderActivity.this, "Campaign scheduled and profiles updated!", Toast.LENGTH_LONG).show();
                    finish();
                }
                @Override
                public void onError(String message) {
                    btnSetReminder.setEnabled(true);
                    Toast.makeText(ReminderActivity.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Citizens create reminders for their own family members in that category
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) return;

            db.collection("family_members")
                .whereEqualTo("userId", uid)
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        // Fallback: Just schedule local notification if no family members found
                        scheduleLocalNotification(vaccine);
                        Toast.makeText(this, "Local reminder set (No matching family members found)", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    AtomicInteger count = new AtomicInteger(snapshots.size());
                    for (QueryDocumentSnapshot doc : snapshots) {
                        repository.addReminder(doc.getId(), vaccine, date, new VaccinationRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                if (count.decrementAndGet() == 0) {
                                    scheduleLocalNotification(vaccine);
                                    Toast.makeText(ReminderActivity.this, "Family reminders synced!", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }
                            @Override
                            public void onError(String msg) {
                                if (count.decrementAndGet() == 0) {
                                    scheduleLocalNotification(vaccine);
                                    finish();
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    btnSetReminder.setEnabled(true);
                    Toast.makeText(this, "Sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
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
