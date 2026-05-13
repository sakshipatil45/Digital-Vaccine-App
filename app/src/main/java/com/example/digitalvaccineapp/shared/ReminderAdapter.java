package com.example.digitalvaccineapp.shared;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.digitalvaccineapp.R;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
    private List<Reminder> reminderList;

    public ReminderAdapter(List<Reminder> reminderList) {
        this.reminderList = reminderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vaccination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);
        holder.tvName.setText(reminder.getVaccineName());
        
        String timeInfo = reminder.getReminderTime() != null ? " at " + reminder.getReminderTime() : "";
        holder.tvDate.setText("Due on: " + reminder.getReminderDate() + timeInfo);
        
        if (reminder.getMemberName() != null && !reminder.getMemberName().isEmpty()) {
            String displayName = reminder.getMemberName();
            if (displayName.equalsIgnoreCase("Self")) displayName = "Primary User";
            holder.tvDependent.setText("For: " + displayName);
            holder.tvDependent.setVisibility(View.VISIBLE);
        } else {
            holder.tvDependent.setVisibility(View.GONE);
        }

        if (reminder.getPlace() != null) {
            holder.tvHospital.setText("Location: " + reminder.getPlace());
            holder.tvHospital.setVisibility(View.VISIBLE);
        }
        
        String status = reminder.getStatus() != null ? reminder.getStatus() : "Pending";
        holder.tvStatus.setText(status);
        
        int statusColor = holder.itemView.getContext().getColor(R.color.primary_purple);
        if (status.equalsIgnoreCase("Completed")) {
            statusColor = holder.itemView.getContext().getColor(R.color.primary_blue);
        }
        holder.vStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));

        // Hide administrative buttons for citizen view
        holder.btnEdit.setVisibility(View.GONE);
        holder.btnDelete.setVisibility(View.GONE);
        holder.btnReminder.setVisibility(View.GONE);
        holder.tvDose.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvStatus, tvHospital, tvDose, tvDependent;
        View vStatusBadge;
        View btnEdit, btnDelete, btnReminder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvVaccineName);
            tvDate = itemView.findViewById(R.id.tvDateTaken);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvHospital = itemView.findViewById(R.id.tvHospitalName);
            tvDose = itemView.findViewById(R.id.tvDoseInfo);
            tvDependent = itemView.findViewById(R.id.tvDependentName);
            vStatusBadge = itemView.findViewById(R.id.vStatusBadge);
            btnEdit = itemView.findViewById(R.id.btnEditVax);
            btnDelete = itemView.findViewById(R.id.btnDeleteVax);
            btnReminder = itemView.findViewById(R.id.btnSetReminder);
        }
    }
}
