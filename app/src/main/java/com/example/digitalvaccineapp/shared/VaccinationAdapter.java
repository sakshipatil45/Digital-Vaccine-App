package com.example.digitalvaccineapp.shared;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.shared.Vaccination;
import android.widget.ImageButton;
import java.util.List;

public class VaccinationAdapter extends RecyclerView.Adapter<VaccinationAdapter.ViewHolder> {
    private List<Vaccination> vaccinationList;
    private OnVaccinationClickListener listener;
    private boolean isReadOnly;

    public interface OnVaccinationClickListener {
        void onEditClick(Vaccination vaccination);
        void onDeleteClick(Vaccination vaccination);
        void onItemClick(Vaccination vaccination);
        void onReminderClick(Vaccination vaccination);
    }

    public VaccinationAdapter(List<Vaccination> vaccinationList, OnVaccinationClickListener listener) {
        this(vaccinationList, listener, false);
    }

    public VaccinationAdapter(List<Vaccination> vaccinationList, OnVaccinationClickListener listener, boolean isReadOnly) {
        this.vaccinationList = vaccinationList;
        this.listener = listener;
        this.isReadOnly = isReadOnly;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vaccination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vaccination vaccination = vaccinationList.get(position);
        holder.tvName.setText(vaccination.getVaccineName());
        holder.tvHospital.setText(vaccination.getHospitalName());
        holder.tvDependent.setText("For: " + vaccination.getDependentName());
        holder.tvDate.setText(vaccination.getDateTaken());
        holder.tvDose.setText("Dose: " + vaccination.getDoseNumber());
        
        String status = vaccination.getStatus().toLowerCase();
        holder.tvStatus.setText(status);
        
        // Dynamic Status Color
        int statusColor;
        if (status.contains("done") || status.contains("completed")) {
            statusColor = holder.itemView.getContext().getColor(R.color.primary_teal);
        } else if (status.contains("pending")) {
            statusColor = holder.itemView.getContext().getColor(R.color.pending_orange);
        } else {
            statusColor = holder.itemView.getContext().getColor(R.color.text_sub);
        }
        
        holder.vStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));

        // Manage Read-Only Mode
        if (isReadOnly) {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        }

        // Show bell icon only for pending records
        if (status.contains("pending")) {
            holder.btnReminder.setVisibility(View.VISIBLE);
        } else {
            holder.btnReminder.setVisibility(View.GONE);
        }

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onItemClick(vaccination));
            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(vaccination));
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(vaccination));
            holder.btnReminder.setOnClickListener(v -> listener.onReminderClick(vaccination));
        }
    }

    @Override
    public int getItemCount() {
        return vaccinationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvStatus, tvHospital, tvDose, tvDependent;
        View vStatusBadge;
        ImageButton btnEdit, btnDelete, btnReminder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvVaccineName);
            tvHospital = itemView.findViewById(R.id.tvHospitalName);
            tvDependent = itemView.findViewById(R.id.tvDependentName);
            tvDate = itemView.findViewById(R.id.tvDateTaken);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDose = itemView.findViewById(R.id.tvDoseInfo);
            vStatusBadge = itemView.findViewById(R.id.vStatusBadge);
            btnEdit = itemView.findViewById(R.id.btnEditVax);
            btnDelete = itemView.findViewById(R.id.btnDeleteVax);
            btnReminder = itemView.findViewById(R.id.btnSetReminder);
        }
    }
}
