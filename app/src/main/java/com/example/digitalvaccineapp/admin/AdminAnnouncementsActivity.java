package com.example.digitalvaccineapp.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAnnouncementsActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etMessage, etDate, etTime, etLocation;
    private MaterialButton btnSend;
    private RecyclerView rvRecent;
    private FirebaseFirestore db;
    private List<Map<String, Object>> announcementList;
    private AnnouncementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_announcements);

        db = FirebaseFirestore.getInstance();
        etTitle = findViewById(R.id.etAnnounceTitle);
        etMessage = findViewById(R.id.etAnnounceMessage);
        btnSend = findViewById(R.id.btnSendBroadcast);
        etDate = findViewById(R.id.etAnnounceDate);
        etTime = findViewById(R.id.etAnnounceTime);
        etLocation = findViewById(R.id.etAnnounceLocation);
        rvRecent = findViewById(R.id.rvRecentAnnouncements);

        Toolbar toolbar = findViewById(R.id.toolbarAnnouncements);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Broadcast Center");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        rvRecent.setLayoutManager(new LinearLayoutManager(this));
        announcementList = new ArrayList<>();
        adapter = new AnnouncementAdapter(announcementList);
        rvRecent.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendBroadcast());

        loadRecentAnnouncements();
        handlePrefills();
    }

    private void handlePrefills() {
        if (getIntent() != null) {
            String title = getIntent().getStringExtra("prefill_title");
            String msg = getIntent().getStringExtra("prefill_message");
            String date = getIntent().getStringExtra("prefill_date");
            String time = getIntent().getStringExtra("prefill_time");
            String loc = getIntent().getStringExtra("prefill_location");

            if (title != null) etTitle.setText(title);
            if (msg != null) etMessage.setText(msg);
            if (date != null) etDate.setText(date);
            if (time != null) etTime.setText(time);
            if (loc != null) etLocation.setText(loc);
        }
    }

    private void showDatePicker() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            etDate.setText(date);
        }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String amPm = hourOfDay < 12 ? "AM" : "PM";
            int hour = hourOfDay % 12;
            if (hour == 0) hour = 12;
            String time = String.format("%02d:%02d %s", hour, minute, amPm);
            etTime.setText(time);
        }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), false).show();
    }

    private void sendBroadcast() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Title and Message are required", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setEnabled(false);
        Map<String, Object> announcement = new HashMap<>();
        announcement.put("title", title);
        announcement.put("message", message);
        announcement.put("date", date);
        announcement.put("time", time);
        announcement.put("location", location);
        announcement.put("timestamp", com.google.firebase.Timestamp.now());
        announcement.put("sender", "Admin");

        db.collection("announcements").add(announcement)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Broadcast sent successfully!", Toast.LENGTH_LONG).show();
                etTitle.setText("");
                etMessage.setText("");
                etDate.setText("");
                etTime.setText("");
                etLocation.setText("");
                btnSend.setEnabled(true);
            })
            .addOnFailureListener(e -> {
                btnSend.setEnabled(true);
                Toast.makeText(this, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loadRecentAnnouncements() {
        db.collection("announcements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener((snapshots, e) -> {
                if (e != null || snapshots == null) return;
                announcementList.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    announcementList.add(doc.getData());
                }
                adapter.notifyDataSetChanged();
            });
    }

    private class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.VH> {
        private List<Map<String, Object>> list;
        public AnnouncementAdapter(List<Map<String, Object>> list) { this.list = list; }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_announcement, p, false));
        }

        @Override public void onBindViewHolder(@NonNull VH h, int p) {
            Map<String, Object> item = list.get(p);
            h.title.setText((String) item.get("title"));
            h.msg.setText((String) item.get("message"));
            
            String date = (String) item.get("date");
            String time = (String) item.get("time");
            String location = (String) item.get("location");
            
            StringBuilder info = new StringBuilder();
            if (!TextUtils.isEmpty(date)) info.append(date);
            if (!TextUtils.isEmpty(time)) info.append(" | ").append(time);
            if (!TextUtils.isEmpty(location)) info.append(" | ").append(location);
            
            if (info.length() > 0) {
                h.eventInfo.setText(info.toString());
                h.eventInfo.setVisibility(View.VISIBLE);
            } else {
                h.eventInfo.setVisibility(View.GONE);
            }
            
            Object ts = item.get("timestamp");
            if (ts instanceof com.google.firebase.Timestamp) {
                h.date.setText(((com.google.firebase.Timestamp) ts).toDate().toLocaleString());
            }
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title, msg, date, eventInfo;
            public VH(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.tvAnnounceItemTitle);
                msg = v.findViewById(R.id.tvAnnounceItemMessage);
                date = v.findViewById(R.id.tvAnnounceItemDate);
                eventInfo = v.findViewById(R.id.tvAnnounceItemEventInfo);
            }
        }
    }
}
