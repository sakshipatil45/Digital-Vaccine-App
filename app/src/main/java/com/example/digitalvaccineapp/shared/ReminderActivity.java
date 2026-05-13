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
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.network.VaccinationRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.Map;

public class ReminderActivity extends AppCompatActivity {

    private AutoCompleteTextView spinnerVaccine;
    private TextInputEditText etDate, etTime, etPlace;
    private MaterialButton btnSetReminder;
    private java.util.Calendar selectedCalendar;
    private VaccinationRepository repository;
    private com.google.firebase.firestore.FirebaseFirestore db;
    private AutoCompleteTextView spinnerCategory;
    private TextInputLayout layoutCategory;
    private String userRole = "citizen";
    private String forceMemberId = null;
    private String forceMemberName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        db = FirebaseFirestore.getInstance();
        repository = new VaccinationRepository(this);
        createNotificationChannel();

        spinnerVaccine = findViewById(R.id.spinnerReminderVaccine);
        spinnerCategory = findViewById(R.id.spinnerReminderCategory);
        layoutCategory = findViewById(R.id.layoutCategorySelection);
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
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getBooleanExtra("isAdmin", false)) {
                userRole = "admin";
                com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
                if (toolbar != null) toolbar.setTitle("New Vaccination Schedule");
                btnSetReminder.setText("Create Campaign Schedule");
                return;
            }

            forceMemberId = intent.getStringExtra("force_member_id");
            forceMemberName = intent.getStringExtra("force_patient");
            
            if (forceMemberId != null) {
                // Citizen setting reminder for a specific member from records
                layoutCategory.setVisibility(View.GONE);
                com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
                if (toolbar != null) toolbar.setTitle("Set Reminder for " + forceMemberName);
                btnSetReminder.setText("Save Personal Reminder");
                
                String vName = intent.getStringExtra("force_vaccine");
                if (vName != null) {
                    spinnerVaccine.setText(vName, false);
                    spinnerVaccine.setEnabled(false); // Locked to this vaccine
                }
                
                String vHospital = intent.getStringExtra("force_hospital");
                if (vHospital != null) etPlace.setText(vHospital);

                String vDate = intent.getStringExtra("force_date");
                if (vDate != null) {
                    if (vDate.contains(" ")) {
                        String[] parts = vDate.split(" ");
                        etDate.setText(parts[0]);
                        etTime.setText(parts[1]);
                    } else {
                        etDate.setText(vDate);
                        etTime.setText("09:00 AM");
                    }
                    
                    // Try to update the calendar for the alarm logic
                    try {
                        String[] dParts = etDate.getText().toString().split("-");
                        if (dParts.length == 3) {
                            selectedCalendar = Calendar.getInstance();
                            selectedCalendar.set(Calendar.YEAR, Integer.parseInt(dParts[0]));
                            selectedCalendar.set(Calendar.MONTH, Integer.parseInt(dParts[1]) - 1);
                            selectedCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dParts[2]));
                        }
                    } catch (Exception ignore) {}
                }
            }
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists() && userRole.equals("citizen")) {
                userRole = doc.getString("role");
            }
        });
    }

    private void setupVaccineAdapter() {
        String[] vaccines = {"Covaxin", "Covishield", "Sputnik V", "Pfizer", "Moderna", "Other"};
        spinnerVaccine.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vaccines));
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
            Toast.makeText(this, "Please fill Date, Time, and Place", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSetReminder.setEnabled(false);

        if ("admin".equalsIgnoreCase(userRole)) {
            String category = spinnerCategory.getText().toString();
            if (category.isEmpty()) {
                Toast.makeText(this, "Please select an age group", Toast.LENGTH_SHORT).show();
                btnSetReminder.setEnabled(true);
                return;
            }
            repository.scheduleGlobalVaccination(category, vaccine, date, time, place, new VaccinationRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ReminderActivity.this, "Campaign Created!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                @Override
                public void onError(String message) {
                    btnSetReminder.setEnabled(true);
                    Toast.makeText(ReminderActivity.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Citizen Mode
            if (forceMemberId != null) {
                // Specific member reminder from Records page
                repository.addReminder(forceMemberId, forceMemberName, vaccine, date, time, place, new VaccinationRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        scheduleLocalNotification(vaccine, forceMemberName);
                        Toast.makeText(ReminderActivity.this, "Reminder set for " + forceMemberName, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    @Override
                    public void onError(String msg) {
                        btnSetReminder.setEnabled(true);
                        Toast.makeText(ReminderActivity.this, "Error: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // General reminder from Dashboard (matches by category)
                String category = spinnerCategory.getText().toString();
                if (category.isEmpty()) {
                    Toast.makeText(this, "Please select category", Toast.LENGTH_SHORT).show();
                    btnSetReminder.setEnabled(true);
                    return;
                }
                processCategoryReminders(category, vaccine, date, time, place);
            }
        }
    }

    private void processCategoryReminders(String category, String vaccine, String date, String time, String place) {
        String uid = FirebaseAuth.getInstance().getUid();
        db.collection("family_members").whereEqualTo("userId", uid).whereEqualTo("category", category).get()
            .addOnSuccessListener(snapshots -> {
                if (snapshots.isEmpty()) {
                    scheduleLocalNotification(vaccine, "Me");
                    finish();
                    return;
                }
                AtomicInteger count = new AtomicInteger(snapshots.size());
                for (QueryDocumentSnapshot doc : snapshots) {
                    String mName = doc.getString("name");
                    repository.addReminder(doc.getId(), mName, vaccine, date, time, place, new VaccinationRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            if (count.decrementAndGet() == 0) {
                                scheduleLocalNotification(vaccine, mName);
                                finish();
                            }
                        }
                        @Override
                        public void onError(String msg) {
                            if (count.decrementAndGet() == 0) finish();
                        }
                    });
                }
            });
    }

    private void scheduleLocalNotification(String vaccine, String memberName) {
        String time = etTime.getText().toString();
        String place = etPlace.getText().toString();
        String date = etDate.getText().toString();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("vaccineName", vaccine);
        intent.putExtra("memberName", memberName);
        intent.putExtra("time", time);
        intent.putExtra("place", place);
        intent.putExtra("date", date);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, (vaccine + memberName).hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null && selectedCalendar != null) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, selectedCalendar.getTimeInMillis(), pendingIntent);
            } catch (SecurityException e) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, selectedCalendar.getTimeInMillis(), pendingIntent);
            }
            NotificationPrefs.saveReminder(this, vaccine + " Reminder", "For " + memberName + " @ " + place, date);
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
