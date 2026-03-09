package com.example.digitalvaccineapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.models.Vaccination;
import java.util.List;

public class VaccinationAdapter extends RecyclerView.Adapter<VaccinationAdapter.ViewHolder> {
    private List<Vaccination> vaccinationList;

    public VaccinationAdapter(List<Vaccination> vaccinationList) {
        this.vaccinationList = vaccinationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Assuming a layout file item_vaccination.xml exists
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vaccination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vaccination vaccination = vaccinationList.get(position);
        holder.tvName.setText(vaccination.getVaccineName());
        holder.tvHospital.setText(vaccination.getHospitalName());
        holder.tvDate.setText(vaccination.getDateTaken());
        holder.tvDose.setText("Dose: " + vaccination.getDoseNumber());
        
        String status = vaccination.getStatus().toLowerCase();
        holder.tvStatus.setText(status);
        
        // Dynamic Status Color
        int statusColor;
        if (status.contains("done") || status.contains("completed")) {
            statusColor = holder.itemView.getContext().getColor(R.color.status_done);
        } else if (status.contains("pending")) {
            statusColor = holder.itemView.getContext().getColor(R.color.status_pending);
        } else {
            statusColor = holder.itemView.getContext().getColor(R.color.status_upcoming);
        }
        
        holder.vStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));
    }

    @Override
    public int getItemCount() {
        return vaccinationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvStatus, tvHospital, tvDose;
        View vStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvVaccineName);
            tvHospital = itemView.findViewById(R.id.tvHospitalName);
            tvDate = itemView.findViewById(R.id.tvDateTaken);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDose = itemView.findViewById(R.id.tvDoseInfo);
            vStatusBadge = itemView.findViewById(R.id.vStatusBadge);
        }
    }
}
