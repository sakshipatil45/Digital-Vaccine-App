package com.example.digitalvaccineapp.shared;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.digitalvaccineapp.R;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private com.google.firebase.firestore.FirebaseFirestore db;
    private java.util.List<NotificationItem> notificationList;
    private NotificationAdapter adapter;
    private RecyclerView rvNotifications;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new java.util.ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        rvNotifications.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        notificationList.clear();
        
        // 1. Load local reminders
        List<NotificationItem> localList = NotificationPrefs.getReminders(this);
        notificationList.addAll(localList);

        // 2. Fetch global announcements from Firestore
        db.collection("announcements")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : task.getResult()) {
                        String title = doc.getString("title");
                        String message = doc.getString("message");
                        String dateStr = doc.getString("date");
                        if (dateStr == null) dateStr = "Broadcast Alert";
                        
                        notificationList.add(0, new NotificationItem(doc.getId(), title, message, dateStr));
                    }
                }
                
                updateUI();
            });
    }

    private void updateUI() {
        if (notificationList.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

        private final List<NotificationItem> items;

        NotificationAdapter(List<NotificationItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationItem item = items.get(position);
            holder.tvTitle.setText(item.getTitle());
            holder.tvMessage.setText(item.getMessage());
            holder.tvDate.setText("Scheduled for: " + item.getDate());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvMessage, tvDate;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvDate = itemView.findViewById(R.id.tvDate);
            }
        }
    }
}
