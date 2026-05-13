package com.example.digitalvaccineapp.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.digitalvaccineapp.R;
import java.util.List;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.CampaignViewHolder> {

    private List<Campaign> campaignList;

    public interface OnCampaignReminderListener {
        void onSendReminder(Campaign campaign);
    }

    private OnCampaignReminderListener listener;

    public CampaignAdapter(List<Campaign> campaignList, OnCampaignReminderListener listener) {
        this.campaignList = campaignList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CampaignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_campaign, parent, false);
        return new CampaignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CampaignViewHolder holder, int position) {
        Campaign campaign = campaignList.get(position);
        holder.tvCampaignVaccine.setText(campaign.getVaccineName());
        holder.tvCampaignCategory.setText("Category: " + campaign.getTargetCategory());
        holder.tvCampaignDate.setText(campaign.getReminderDate());
        holder.tvCampaignTime.setText(campaign.getTime());
        holder.tvCampaignPlace.setText(campaign.getPlace());

        holder.btnSendReminder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSendReminder(campaign);
            }
        });
    }

    @Override
    public int getItemCount() {
        return campaignList.size();
    }

    public void updateData(List<Campaign> newList) {
        this.campaignList = newList;
        notifyDataSetChanged();
    }

    static class CampaignViewHolder extends RecyclerView.ViewHolder {
        TextView tvCampaignVaccine, tvCampaignCategory, tvCampaignDate, tvCampaignTime, tvCampaignPlace;
        android.widget.ImageButton btnSendReminder;

        public CampaignViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCampaignVaccine = itemView.findViewById(R.id.tvCampaignVaccine);
            tvCampaignCategory = itemView.findViewById(R.id.tvCampaignCategory);
            tvCampaignDate = itemView.findViewById(R.id.tvCampaignDate);
            tvCampaignTime = itemView.findViewById(R.id.tvCampaignTime);
            tvCampaignPlace = itemView.findViewById(R.id.tvCampaignPlace);
            btnSendReminder = itemView.findViewById(R.id.btnSendReminder);
        }
    }
}
