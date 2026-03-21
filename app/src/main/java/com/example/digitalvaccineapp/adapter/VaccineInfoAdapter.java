package com.example.digitalvaccineapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.digitalvaccineapp.R;
import com.example.digitalvaccineapp.models.VaccineInfo;
import java.util.List;

public class VaccineInfoAdapter extends RecyclerView.Adapter<VaccineInfoAdapter.ViewHolder> {
    private List<VaccineInfo> vaccineInfoList;

    public VaccineInfoAdapter(List<VaccineInfo> vaccineInfoList) {
        this.vaccineInfoList = vaccineInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vaccine_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VaccineInfo info = vaccineInfoList.get(position);
        holder.tvName.setText(info.getName());
        holder.tvDescription.setText(info.getDescription());
        holder.tvDosage.setText("Dosage: " + info.getDosage());
        holder.tvAge.setText("Age Group: " + info.getAgeGroup());
        holder.ivIcon.setImageResource(info.getIconResId());
        holder.ivIcon.setColorFilter(holder.itemView.getContext().getColor(R.color.primary_teal));
    }

    @Override
    public int getItemCount() {
        return vaccineInfoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvDosage, tvAge;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvInfoName);
            tvDescription = itemView.findViewById(R.id.tvInfoDescription);
            tvDosage = itemView.findViewById(R.id.tvInfoDosage);
            tvAge = itemView.findViewById(R.id.tvInfoAge);
            ivIcon = itemView.findViewById(R.id.ivInfoIcon);
        }
    }
}
